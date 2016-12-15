/**
 * Superuser
 * Copyright (C) 2016 Pierre-Hugues Husson (phhusson)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.koushikdutta.superuser.db;

import java.util.Date;

import com.koushikdutta.superuser.R;

public class UidPolicy extends UidCommand {
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";
    public static final String INTERACTIVE = "interactive";

    public String policy;
    public int until;
    public boolean logging = true;
    public boolean notification = true;

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy){
        this.policy = policy;
    }

    public Date getUntilDate() {
        return new Date((long)until * 1000);
    }

    public int getPolicyResource() {
        if (ALLOW.equals(policy))
            return R.string.allow;

        else if (INTERACTIVE.equals(policy))
            return R.string.interactive;

        return R.string.deny;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof UidPolicy) {

            UidPolicy policy = (UidPolicy) o;

            return (policy.getPolicy() == null ? this.policy == null : policy.getPolicy().equals(this.policy)) &&
                    policy.until == this.until &&
                    policy.logging == this.logging &&
                    policy.notification == this.notification;
        }

        return false;
    }

    @Override
    public int hashCode() {

        int prime = 11;

        int result = 1;

        result = prime * result + (policy == null ? 0 : policy.hashCode());

        result = prime * result + until;

        result = prime * result + (logging ? 1 : 0);

        result = prime + result + (notification ? 1 : 0);

        return result;
    }
}