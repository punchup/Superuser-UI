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

public class LogEntry extends UidCommand {
    public long id;
    public String action;
    public int date;

    public Date getDate() {
        return new Date((long)date * 1000);
    }

    public int getActionResource() {

        if (UidPolicy.ALLOW.equals(action))
            return R.string.allow;

        else if (UidPolicy.INTERACTIVE.equals(action))
            return R.string.interactive;

        return R.string.deny;
    }
}
