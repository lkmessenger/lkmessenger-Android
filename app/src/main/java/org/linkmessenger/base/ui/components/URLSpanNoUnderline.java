/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.linkmessenger.base.ui.components;

import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.linkmessenger.base.ui.AndroidUtilities;
import org.linkmessenger.base.ui.components.browser.Browser;
import org.linkmessenger.profile.view.activities.ProfileActivity;
import org.linkmessenger.utils.PostUtil;
import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.R;


public class URLSpanNoUnderline extends URLSpan {

    private boolean forceNoUnderline = false;
    private TextStyleSpan.TextStyleRun style;

    // Used to label video timestamps
    public String label;

    public URLSpanNoUnderline(String url) {
        this(url, null);
    }

    public URLSpanNoUnderline(String url, boolean forceNoUnderline) {
        this(url, null);
        this.forceNoUnderline = forceNoUnderline;
    }

    public URLSpanNoUnderline(String url, TextStyleSpan.TextStyleRun run) {
        super(url != null ? url.replace('\u202E', ' ') : url);
        style = run;
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        if(url.startsWith("@")){
            String sub = url.substring(1);
            Intent intent = ProfileActivity.Companion.newIntent(ApplicationContext.applicationContext, null, sub, null);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ApplicationContext.applicationContext.startActivity(intent);
        }else if(!url.startsWith("https://linkm.me/")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ApplicationContext.applicationContext.startActivity(browserIntent);
        }else {
            PostUtil.INSTANCE.handlePostUrl(ApplicationContext.applicationContext, url);
        }
//        if (url.startsWith("@")) {
//            Uri uri = Uri.parse("https://t.me/" + url.substring(1));
//            Browser.openUrl(widget.getContext(), uri);
//        } else {
//            Browser.openUrl(widget.getContext(), url);
//        }
    }

    @Override
    public void updateDrawState(TextPaint p) {
        String url = getURL();
        if (url.startsWith("@")) {
            p.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            p.linkColor = ContextCompat.getColor(ApplicationContext.applicationContext, R.color.signal_colorPrimary);
        }
        int l = p.linkColor;
        int c = p.getColor();
        super.updateDrawState(p);
        if (style != null) {
            style.applyStyle(p);
        }
        p.setUnderlineText(l == c && !forceNoUnderline);
    }
}
