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

package com.koushikdutta.superuser;

import android.graphics.drawable.Drawable;

import com.koushikdutta.superuser.db.UidPolicy;

class ListItem {

    private String item1, item2, item3;

    private Drawable icon;

    private UidPolicy policy;


    ListItem(UidPolicy policy, String item1, String item2, String item3, Drawable icon) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.icon = icon;

        this.policy = policy;
    }

    UidPolicy getPolicy() {
        return policy;
    }

    void setPolicy(UidPolicy policy) {
        this.policy = policy;
    }

    String getItem1() {
        return item1;
    }

    void setItem1(String item1) {
        this.item1 = item1;
    }

    String getItem2() {
        return item2;
    }

    void setItem2(String item2) {
        this.item2 = item2;
    }

    String getItem3() {
        return item3;
    }

    void setItem3(String item3) {
        this.item3 = item3;
    }

    Drawable getIcon() {
        return icon;
    }

    void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof ListItem) {

            ListItem item = (ListItem) o;

            return (item.getPolicy() == null ? policy == null : item.getPolicy().equals(this.policy)) &&
                    (item.getItem1() == null ? item1 == null : item.getItem1().equals(this.item1)) &&
                    (item.getItem2() == null ? item2 == null : item.getItem2().equals(this.item2)) &&
                    (item.getItem3() == null ? item3 == null : item.getItem3().equals(this.item3)) &&
                    (item.getIcon() == null ? icon == null : item.getIcon().equals(this.icon));
        }

        return false;
    }

    @Override
    public int hashCode() {

        int prime = 11;

        int result = 1;

        result = prime * result + (policy == null ? 0 : policy.hashCode());

        result = prime * result + (item1 == null ? 0 : item1.hashCode());

        result = prime * result + (item2 == null ? 0 : item2.hashCode());

        result = prime * result + (item3 == null ? 0 : item3.hashCode() );

        result = prime * result + (icon == null ? 0 : icon.hashCode());

        return result;
    }
}