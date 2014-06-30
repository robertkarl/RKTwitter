package com.codepath.apps.RKTwitterClient.util;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

public class Util {

    static Typeface robotoMedium;

    public static Typeface getRobotoMedium(Context context) {
        if (robotoMedium == null) {
            robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto/Roboto-Medium.ttf");
        }
        return robotoMedium;
    }


    public static void setupTextviewContents(View parentView, int textViewID, String textValue) {
        TextView tvUserName = (TextView) parentView.findViewById(textViewID);
        tvUserName.setText(textValue);
    }


    /**
     * Update the drawable, bypassing Android null drawable bug.
     * Re-enable/disable your title after setting background
     */
    public static void setActionBarDrawable(ActionBar actionBar, ColorDrawable drawable) {
        actionBar.setBackgroundDrawable(drawable);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
    }
}
