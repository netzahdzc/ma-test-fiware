package com.example.android.ollintest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ollintest.R;
import com.example.android.ollintest.util.ImageUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class WizardWalkingTestFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;
    private ImageView image;
    private TextView text;

    public static WizardWalkingTestFragment newInstance(int position) {
        WizardWalkingTestFragment f = new WizardWalkingTestFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    public static void initImageLoader(Context context) {
        // Initialize ImageLoader with configuration.
        if (!ImageLoader.getInstance().isInited()) {
            // This configuration tuning is custom. You can tune every option, you may tune some of them,
            // or you can create default configuration by
            //  ImageLoaderConfiguration.createDefault(this);
            // method.
            ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
            config.threadPriority(Thread.NORM_PRIORITY - 2);
            config.denyCacheImageMultipleSizesInMemory();
            config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
            config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
            config.tasksProcessingOrder(QueueProcessingType.LIFO);
            config.writeDebugLogs(); // Remove for release app

            ImageLoader.getInstance().init(config.build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wizard_social,
                container, false);
        image = (ImageView) rootView
                .findViewById(R.id.fragment_wizard_social_image);
        text = (TextView) rootView
                .findViewById(R.id.fragment_wizard_social_second_text);

        initImageLoader(getContext());

        // Prueba de marcha: The Timed Up and Go (TUG) Test
        switch (position) {
            case 0: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_19, null);
                text.setText(getResources().getString(R.string.tug_test_warning));
            }
            break;
            case 1: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_15, null);
                text.setText(getResources().getString(R.string.tug_test_preparation));
            }
            break;
            case 2: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_1, null);
                text.setText(getResources().getString(R.string.tug_test_start));
            }
            break;
            case 3: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_2, null);
                text.setText(getResources().getString(R.string.tug_test_get_up));
            }
            break;
            case 4: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_3, null);
                text.setText(getResources().getString(R.string.tug_test_walk));
            }
            break;
            case 5: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_4, null);
                text.setText(getResources().getString(R.string.tug_test_turn_around));
            }
            break;
            case 6: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_5, null);
                text.setText(getResources().getString(R.string.tug_test_return));
            }
            break;
            case 7: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_6, null);
                text.setText(getResources().getString(R.string.tug_test_sit));
            }
            break;
            default: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_7, null);
                text.setText(getResources().getString(R.string.tug_test_stop));
            }
        }

        ViewCompat.setElevation(rootView, 50);
        return rootView;
    }
}