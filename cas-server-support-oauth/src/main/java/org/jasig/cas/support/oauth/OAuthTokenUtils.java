/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.support.oauth;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * OAuth Access Token Utilities
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public class OAuthTokenUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenUtils.class);

    public static OAuthToken getAccessToken(final HttpServletRequest request, final CipherExecutor cipherExecutor)
            throws RuntimeException {
        final String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization) || !authorization.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN + " ")) {
            LOGGER.debug("Missing bearer access token");
            throw new AccessTokenMissingException();
        }

        final String jwtToken = authorization.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
        final OAuthToken token = readToken(cipherExecutor, jwtToken);
        if (token == null) {
            LOGGER.debug("Could not read bearer access token");
            throw new AccessTokenInvalidException();
        }
        return token;
    }

    public static OAuthToken getToken(final HttpServletRequest request, final CipherExecutor cipherExecutor, final String name)
            throws RuntimeException {
        final String jwtToken = request.getParameter(name);
        final OAuthToken token = readToken(cipherExecutor, jwtToken);
        if (token == null) {
            LOGGER.debug("Could not read token [{}]", name);
            throw new TokenInvalidException();
        }
        return token;
    }

    public static Ticket getTicket(final CentralAuthenticationService centralAuthenticationService, final OAuthToken token)
            throws RuntimeException {
        final String ticketId = token.serviceTicketId != null ? token.serviceTicketId : token.ticketGrantingTicketId;
        try {
            return centralAuthenticationService.getTicket(ticketId, Ticket.class);
        } catch (InvalidTicketException e) {
            LOGGER.debug("Invalid or expired ticket [{}]", token);
            throw new TokenInvalidException();
        }
    }

    public static ServiceTicket getServiceTicket(final CentralAuthenticationService centralAuthenticationService,
                                                 final OAuthToken token)
            throws RuntimeException {
        if (token.serviceTicketId == null) {
            final Service service = new SimpleWebApplicationServiceImpl(token.serviceId);
            try {
                return centralAuthenticationService.grantServiceTicket(token.ticketGrantingTicketId, service);
            } catch (TicketException e) {
                LOGGER.debug("Invalid or expired ticket granting ticket [{}] for service [{}]", token.ticketGrantingTicketId, service);
                throw new TokenInvalidException();
            }
        } else {
            try {
                return centralAuthenticationService.getTicket(token.serviceTicketId, Ticket.class);
            } catch (InvalidTicketException e) {
                LOGGER.debug("Invalid or expired service ticket [{}]", token.serviceTicketId);
                throw new TokenInvalidException();
            }
        }
    }

    public static Boolean hasPermission(final Ticket accessTicket, final Ticket ticket) {
        final Authentication accessAuthentication = accessTicket instanceof TicketGrantingTicket ?
                ((TicketGrantingTicket) accessTicket).getAuthentication() : accessTicket.getGrantingTicket().getAuthentication();
        final Principal accessPrincipal = accessAuthentication.getPrincipal();

        final Authentication ticketAuthentication = ticket instanceof TicketGrantingTicket ?
                ((TicketGrantingTicket) ticket).getAuthentication() : ticket.getGrantingTicket().getAuthentication();
        final Principal ticketPrincipal = ticketAuthentication.getPrincipal();

        return accessPrincipal.getId().equals(ticketPrincipal.getId());
    }

    private static OAuthToken readToken(final CipherExecutor cipherExecutor, final String jwtToken) {
        try {
            return OAuthToken.read(cipherExecutor.decode(jwtToken));
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}