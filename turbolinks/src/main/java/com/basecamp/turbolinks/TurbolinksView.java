package com.basecamp.turbolinks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * <p>The custom view to add to your activity layout.</p>
 */
public class TurbolinksView extends FrameLayout {
    private View progressView = null;
    private ImageView screenshotView = null;

    // ---------------------------------------------------
    // Constructors
    // ---------------------------------------------------

    /**
     * <p>Constructor to match FrameLayout.</p>
     *
     * @param context Refer to FrameLayout.
     */
    public TurbolinksView(Context context) {
        super(context);
    }

    /**
     * <p>Constructor to match FrameLayout.</p>
     *
     * @param context Refer to FrameLayout.
     * @param attrs Refer to FrameLayout.
     */
    public TurbolinksView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * <p>Constructor to match FrameLayout.</p>
     *
     * @param context Refer to FrameLayout.
     * @param attrs Refer to FrameLayout.
     * @param defStyleAttr Refer to FrameLayout.
     */
    public TurbolinksView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * <p>Constructor to match FrameLayout.</p>
     *
     * @param context Refer to FrameLayout.
     * @param attrs Refer to FrameLayout.
     * @param defStyleAttr Refer to FrameLayout.
     * @param defStyleRes Refer to FrameLayout.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TurbolinksView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ---------------------------------------------------
    // Package public
    // ---------------------------------------------------

    /**
     * <p>Shows a progress view or a generated screenshot of the webview content (if available)
     * on top of the webview. When advancing to a new url, this indicates that the page is still
     * loading. When resuming an activity in the navigation stack, a screenshot is displayed while the
     * webview is restoring its snapshot.</p>
     * <p>Progress indicator is set to a specified delay before displaying -- a very short delay
     * (like 500 ms) can improve perceived loading time to the user.</p>
     *
     * @param progressView The progressView to display on top of TurbolinksView.
     * @param progressIndicator The progressIndicator to display in the view.
     * @param delay The delay before showing the progressIndicator in the view. The default progress view
     *              is 500 ms.
     */
    void showProgress(final View progressView, final View progressIndicator, int delay) {
        TurbolinksLog.d("showProgress called");

        // Don't show the progress view if a screenshot is available
        if (screenshotView != null) return;

        hideProgress();

        this.progressView = progressView;
        progressView.setClickable(true);
        addView(progressView);

        progressIndicator.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressIndicator.setVisibility(View.VISIBLE);
            }
        }, delay);
    }

    /**
     * <p>Removes the progress view and/or screenshot from the TurbolinksView, so the webview is
     * visible underneath.</p>
     */
    void hideProgress() {
        if (progressView != null) {
            removeView(progressView);
        }

        if (screenshotView != null) {
            removeView(screenshotView);
            screenshotView = null;
            TurbolinksLog.d("Screenshot removed");
        }
    }

    /**
     * <p>Attach the shared webView to the TurbolinksView.</p>
     *
     * @param webView The shared webView.
     */
    void attachWebView(WebView webView) {
        ViewGroup parent = (ViewGroup) webView.getParent();
        if (parent != null && parent instanceof TurbolinksView) {
            ((TurbolinksView) parent).screenshotView();
            parent.removeView(webView);
        }

        // Set the webview background to match the container background
        if (getBackground() instanceof ColorDrawable) {
            webView.setBackgroundColor(((ColorDrawable) getBackground()).getColor());
        }

        addView(webView, 0);
    }

    /**
     * <p>Creates a screenshot of the current webview content and makes it the top visible view.</p>
     */
    private void screenshotView() {
        // Only take a screenshot if the activity is not finishing
        if (getContext() instanceof Activity && ((Activity) getContext()).isFinishing()) return;

        Bitmap screenshot = getScreenshotBitmap();
        if (screenshot == null) return;

        screenshotView = new ImageView(getContext());
        screenshotView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        screenshotView.setClickable(true);
        screenshotView.setImageBitmap(screenshot);

        addView(screenshotView);

        TurbolinksLog.d("Screenshot taken");
    }

    /**
     * <p>Creates a bitmap screenshot of the webview contents from the canvas.</p>
     * @return The screenshot of the webview contents.
     */
    private Bitmap getScreenshotBitmap() {
        if (getWidth() <= 0 || getHeight() <= 0) return null;

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        draw(new Canvas(bitmap));
        return bitmap;
    }
}
