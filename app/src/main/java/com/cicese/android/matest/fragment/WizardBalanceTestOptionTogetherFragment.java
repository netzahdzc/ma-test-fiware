package com.cicese.android.matest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cicese.android.matest.R;
import com.cicese.android.matest.util.ImageUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class WizardBalanceTestOptionTogetherFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;
    private ImageView image;
    private TextView text;

    public static WizardBalanceTestOptionTogetherFragment newInstance(int position) {
        WizardBalanceTestOptionTogetherFragment f = new WizardBalanceTestOptionTogetherFragment();
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

        // The 4-Stage Balance Test
        switch (position) {
            case 0: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_19, null);
                text.setText(getResources().getString(R.string.balance_step_stay_next_elderly));
            }
            break;
            case 1: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_16, null);
                text.setText(getResources().getString(R.string.balance_step_stay_feet_together));
            }
            break;
            case 2: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_12, null);
                text.setText(getResources().getString(R.string.balance_step_hold_elderly));
            }
            break;
            case 3: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_22, null);
                text.setText(getResources().getString(R.string.balance_step_release_elderly));
            }
            break;
            case 4: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_1, null);
                text.setText(getResources().getString(R.string.balance_step_to_start));
            }
            break;
            default: {
                ImageUtil.displayImage(image, "drawable://" + R.drawable.tutorial_19, null);
                text.setText(getResources().getString(R.string.balance_step_if_cannot_hold));
            }
        }

        ViewCompat.setElevation(rootView, 50);
        return rootView;
    }

}