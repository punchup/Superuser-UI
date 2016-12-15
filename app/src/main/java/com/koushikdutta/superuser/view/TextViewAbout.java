package com.koushikdutta.superuser.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewAbout extends TextView {

    public TextViewAbout(Context context) {
        super(context);
        init();
    }

    public TextViewAbout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewAbout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //setTextColor(0xff999999);
        //setLinkTextColor(0xffd1d1d1);

        setMovementMethod(LinkMovementMethod.getInstance());
        setLineSpacing(0, 1.5f);
    }
}
