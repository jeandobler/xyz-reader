package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private int mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
    private ImageView mPhotoView;
    private Toolbar toolbar;


    private Target mTarget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
//        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(context)
//                        .setType("text/plain")
//                        .setText("Some sample text")
//                        .getIntent(), getString(R.string.action_share)));
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }


        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
//        mPager.setOffscreenPageLimit(1);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//                mUpButton.animate()
//                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
//                        .setDuration(300);
//            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    changeImage();
//                    mPagerAdapter.notifyDataSetChanged();
                }
//                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
//                updateUpButtonPosition();
            }
        });

//        mUpButtonContainer = findViewById(R.id.up_container);
//
//        mUpButton = findViewById(R.id.action_up);
//        mUpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onSupportNavigateUp();
//            }
//        });


        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = (int) ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    public void changeImage() {
        mPhotoView = (ImageView) findViewById(R.id.photo);

        final Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch textSwatch = palette.getDarkVibrantSwatch();

                if (textSwatch == null) {
                    Log.e("ErroNaImagem", "Erro");
                    return;
                }

                findViewById(R.id.share_fab).setBackgroundColor(textSwatch.getRgb());

            }
        };


        mTarget = new Target() {
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e("failedException", e.getMessage());
            }

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {


                Animation fadeOut = new AlphaAnimation(0, 1);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(300);
                mPhotoView.startAnimation(fadeOut);

                mPhotoView.setImageBitmap(bitmap);

//                Animation fadeIn = new AlphaAnimation(1, 0);
//                fadeOut.setInterpolator(new AccelerateInterpolator());
//                fadeOut.setDuration(300);
//                mPhotoView.startAnimation(fadeOut);

                new Palette.Builder(bitmap).generate(paletteListener); //Palette.from(bitmap).generate();

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };


        mPhotoView.setTag(mTarget);
        Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).into(mTarget);


    }


    public void getPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                // here's the palette

            }
        });
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(mStartId, false);
        mCursor.moveToPosition(mStartId);


        changeImage();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

//            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
//            if (fragment != null) {
//                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
//                updateUpButtonPosition();
//            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
