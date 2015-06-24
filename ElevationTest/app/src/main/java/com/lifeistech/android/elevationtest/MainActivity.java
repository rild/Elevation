package com.lifeistech.android.elevationtest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ObservableScrollView.Callbacks {

    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    private int mPhotoHeightPixels;
    private float mMaxHeaderElevation;
    private int mHeaderHeightPixels;

    private ObservableScrollView mScrollView;
    private FrameLayout mHeaderLayout;
    private ImageView mHeaderImageView;
    private LinearLayout mContainerLayout;
    private Toolbar mToolbar;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMaxHeaderElevation = getResources().getDimensionPixelSize(R.dimen.element_spacing_normal);

        //インタンス生成
        mScrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        mHeaderLayout = (FrameLayout) findViewById(R.id.headerlayout);
        mHeaderImageView = (ImageView) findViewById(R.id.headerimage);
        mContainerLayout = (LinearLayout) findViewById(R.id.maincontent);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setLogo(R.mipmap.ic_launcher);
        mToolbar.setTitle("エレベーター");
        mToolbar.setTitleTextColor(Color.WHITE);
        // デフォルトのActionBarを適用
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.mipmap.ic_hi);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HI", Toast.LENGTH_SHORT).show();
            }
        });
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    // View生成し終わったタイミングでしかサイズが取得できないので、今回はonWindowFocusChangeでrecomputeをよぶ
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        recomputePhotoAndScrollingMetrics();
    }

    //レイアウトのサイズ変更
    private void recomputePhotoAndScrollingMetrics() {
        //ツールバーの大きさを取得する(決めうちなら、dimenから取得してもいい)
        mHeaderHeightPixels = mToolbar.getHeight();
        //ヘッダーの画像の大きさを設定する
        mPhotoHeightPixels = 0;
        //TODO 通信などで画像を取得する場合は画像があるかどうかを判定する必要がある
        //どうしても取得できない場合は、画面サイズから計算してもいい
        mPhotoHeightPixels = (int) (mHeaderImageView.getWidth() / PHOTO_ASPECT_RATIO);
        //なくてもいいが画面全体の1/3以上だとバランスが悪いため、上限を設ける。
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);

        //画像をいれている親のFrameレイアウトの高さを変更
        ViewGroup.LayoutParams layoutParams;
        layoutParams = mHeaderLayout.getLayoutParams();
        if (layoutParams.height != mPhotoHeightPixels) {
            layoutParams.height = mPhotoHeightPixels;
            mHeaderLayout.setLayoutParams(layoutParams);
        }

        //メインのコンテンツのレイアウトの高さを変更
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerLayout.getLayoutParams();
        if (marginLayoutParams.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            marginLayoutParams.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mContainerLayout.setLayoutParams(marginLayoutParams);
        }
        //スクロール位置の初期化
        onScrollChanged(0, 0); // trigger scroll handling
    }

    // スクロールされた時に呼ばれるメソッド
    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        //ここで動かすのは、ヘッダーToolbarの位置だけでそれ以外は何も動かさない
        int scrollY = mScrollView.getScrollY();
        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mToolbar.setTranslationY(newTop);
        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(getProgress(scrollY, 0, mPhotoHeightPixels), 0), 1);
        }
        //5.0いこうでしか意味ないが、Viewに高さをつけて少し浮いているようにみせる。
        ViewCompat.setElevation(mToolbar, gapFillProgress * mMaxHeaderElevation);
        mHeaderLayout.setTranslationY(scrollY * 0.5f);
    }

    private float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }
        return (value - min) / (float) (max - min);
    }

}
