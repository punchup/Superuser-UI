package com.koushikdutta.superuser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class MirroredTextView extends TextView {
    public MirroredTextView(Context context) {
        super(context);
    }

    public MirroredTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MirroredTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth(), 0);
        canvas.scale(-1, 1);
        super.onDraw(canvas);
    }
}
