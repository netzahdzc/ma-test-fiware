/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cicese.android.matest;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cicese.android.matest.util.Utilities;

import java.util.ArrayList;

/**
 * {@link PatientAdapter} is an {@link ArrayAdapter} that can provide the layout for each list item
 * based on a data source, which is a list of {@link Patient} objects.
 */
public class PatientAdapter extends ArrayAdapter<Patient> {

    private static final int MALE = 1;
    private static final int FEMALE = 2;

    View listItemView;
    long uniqueParticipantId;

    /**
     * Resource ID for the background color for this list of words
     */
    private int mColorResourceId;

    /**
     * Create a new {@link PatientAdapter} object.
     */
    public PatientAdapter(Context context, ArrayList<Patient> words, int colorResourceId) {
        super(context, 0, words);
        mColorResourceId = colorResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        listItemView = convertView;

        // Get the {@link Patient} object located at this position in the list
        Patient currentObject = getItem(position);

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Find the TextView in the list_item.xml layout.
        TextView uniqueTextId = (TextView) listItemView.findViewById(R.id.unique_participant_id);
        // Get the unique id number from the patient object.
        uniqueTextId.setText(Long.toString(currentObject.getUniquePatientId()));
        uniqueParticipantId = Long.valueOf(currentObject.getUniquePatientId());

        // Find the TextView in the list_item.xml layout.
        TextView participantNameTextView = (TextView) listItemView.findViewById(R.id.participant_name);
        // Get the main text (patient name).
        String contentParticipantName = currentObject.getPatientName().length() > 15 ?
                currentObject.getPatientName().substring(0, 12) + "..." :
                currentObject.getPatientName();
        participantNameTextView.setText(contentParticipantName);

        // Find the TextView in the list_item.xml layout.
        TextView participantSurnameTextView = (TextView) listItemView.findViewById(R.id.participant_surname);
        // Get the secondary text (surname of the patient).
        String contentParticipantSurname = currentObject.getPatientSurname().length() > 15 ?
                currentObject.getPatientSurname().substring(0, 12) + "..." :
                currentObject.getPatientSurname();
        participantSurnameTextView.setText(contentParticipantSurname);


        // Find the ImageView in the list_item.xml layout with the ID image.
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.image);
        // Check if an image is provided for this word or not
        if (currentObject.hasImage()) {
            // If an image is available, display the provided image based on the resource ID
            imageView.setImageBitmap(Utilities.getImage(currentObject.getPatientPhotoResourceId()));
            // Make sure the view is visible
            imageView.setVisibility(View.VISIBLE);
        } else {
            // Otherwise hide the ImageView (set visibility to GONE)
//            imageView.setVisibility(View.GONE);

            // If there is no picture, then I set a placeholder
            if (currentObject.getPatientGender() == MALE)
                imageView.setImageResource(R.drawable.profile_m);
            if (currentObject.getPatientGender() == FEMALE)
                imageView.setImageResource(R.drawable.profile_w);
            imageView.setVisibility(View.VISIBLE);
        }

        // Set the theme color for the list item
        View textContainer = listItemView.findViewById(R.id.text_container);
        // Find the color that the resource ID maps to
        int color = ContextCompat.getColor(getContext(), mColorResourceId);
        // Set the background color of the text container View
        textContainer.setBackgroundColor(color);

        /**
         * To include button at patient list
         */
//        Integer[] temp = {uniqueParticipantId};
//        edit_sell.setTag(temp);
//        edit_sell.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                ((MainActivity) getContext()).markASell((Integer[]) v.getTag());
//            }
//        });

        // Return the whole list item layout so that it can be shown in
        return listItemView;
    }

}