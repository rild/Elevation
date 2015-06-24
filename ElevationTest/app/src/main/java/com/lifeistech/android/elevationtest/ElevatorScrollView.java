package com.lifeistech.android.elevationtest;

/**
 * Created by rild on 15/06/24.
 */

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * Created by shunhosaka on 15/05/15.
 */
public class ElevatorScrollView extends ScrollView {
    private ArrayList<OnScrollChangedListener> mCallbacks = new ArrayList<>();
    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;
    private int mPhotoHeightPixels;
    private int mHeaderHeightPixels;
    // HeadeMenu
    private View mHeaderBox;
    //見出しの画像部分
    private View mHeaderImageLayout;
    // コンテンツを入れている部分
    private View mContentLayout;

    public ElevatorScrollView(Context context) {
        super(context);
    }

    public ElevatorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElevatorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * スクロールさせるコンテンツをセットするために必ず呼ぶようにする。
     * @param headerBoxId (Toolbarなどのヘッダ-バーの部分で独自にスクールさせる部分のID)
     * @param headerImageLayoutId (ImageViewが入った親レイアウトのID)
     * @param contentLayoutId (メインコンテンツの入ったレイアウトのID)
     * @return
     */
    public void setContentsView(int headerBoxId, int headerImageLayoutId, int contentLayoutId) {
        setContentsView(findViewById(headerBoxId), findViewById(headerImageLayoutId), findViewById(contentLayoutId));
    }

    public void setContentsView(View headerBox, View headerImageLayout, View contentLayout) {
        mHeaderBox = headerBox;
        mHeaderImageLayout = headerImageLayout;
        mContentLayout = contentLayout;
        recomputePhotoAndScrollingMetrics();
    }

    //レイアウトのサイズ変更
    private void recomputePhotoAndScrollingMetrics() {
        //ツールバーの大きさを取得する(決めうちなら、dimenから取得してもいい)
        mHeaderHeightPixels = mHeaderBox.getHeight();
        //ヘッダーの画像の大きさを設定する
        mPhotoHeightPixels = 0;
        mPhotoHeightPixels = (int) (getWidth() / PHOTO_ASPECT_RATIO);
        //なくてもいいが画面全体の1/3以上だとバランスが悪いため、上限を設ける。
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, getHeight() * 2 / 3);

        //画像をいれている親のFrameレイアウトの高さを変更
        ViewGroup.LayoutParams layoutParams;
        layoutParams = mHeaderImageLayout.getLayoutParams();
        if (layoutParams.height != mPhotoHeightPixels) {
            layoutParams.height = mPhotoHeightPixels;
            mHeaderImageLayout.setLayoutParams(layoutParams);
        }
        //メインのコンテンツのレイアウトの高さを変更
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContentLayout.getLayoutParams();
        if (marginLayoutParams.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            marginLayoutParams.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mContentLayout.setLayoutParams(marginLayoutParams);
        }
        //スクロール位置の初期化
        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        onScrollChanged(l - oldl, t - oldt);
        for (OnScrollChangedListener callback : mCallbacks) {
            callback.onScrollChanged(l - oldl, t - oldt);
        }
    }

    public void onScrollChanged(int deltaX, int deltaY) {
        //ここで動かすのは、ヘッダーToolbarの位置だけでそれ以外は何も動かさない
        int scrollY = getScrollY();
        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);
        mHeaderImageLayout.setTranslationY(scrollY * 0.5f);
    }

    public void addCallbacks(OnScrollChangedListener listener) {
        if (!mCallbacks.contains(listener)) {
            mCallbacks.add(listener);
        }
    }

    public static interface OnScrollChangedListener {
        public void onScrollChanged(int deltaX, int deltaY);
    }
}

