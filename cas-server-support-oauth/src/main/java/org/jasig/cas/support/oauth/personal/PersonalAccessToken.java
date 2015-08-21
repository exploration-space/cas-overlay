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
package org.jasig.cas.support.oauth.personal;

import java.util.Set;

/**
 * OAuth Scope
 *
 * @author Michael Haselton
 * @since 4.1.0
 */
public final class PersonalAccessToken {
    private final String id;
    private final String principalId;
    private final Set<String> scopes;

    public PersonalAccessToken(final String id, final String principalId, final Set<String> scopes) {
        this.id = id;
        this.principalId = principalId;
        this.scopes = scopes;
    }

    public String getId() {
        return this.id;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}