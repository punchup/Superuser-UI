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

package com.koushikdutta.superuser.helper;

import android.content.Context;
import android.util.Log;

public class SuHelper {

    public static String CURRENT_VERSION = "17";

    public static void checkSu(Context context) throws Exception {
        Process p = Runtime.getRuntime().exec("su -v");

        String result = Settings.readToEnd(p.getInputStream());

        Log.i("Superuser", "Result: " + result);

        if (0 != p.waitFor())
            throw new Exception("non zero result");

        if (result == null)
            throw new Exception("no data");

        if (!result.contains(context.getPackageName()))
            throw new Exception("unknown su");

        String[] parts = result.split(" ");

        if (!CURRENT_VERSION.equals(parts[0]))
            throw new Exception("binary is old");
    }
}
