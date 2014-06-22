package com.codepath.apps.RKTwitterClient.util;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

public class Util {

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
