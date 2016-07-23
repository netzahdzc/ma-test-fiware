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
package com.example.android.ollintest;

public class Patient {

    /**
     * Unique id for the patient
     */
    private long mUniquePatientId;

    /**
     * Name of patient
     */
    private String mPatientName;

    /**
     * Last name of patient
     */
    private String mPatientSurname;

    /**
     * Sex of patient
     */
    private String mPatientGender;

    /**
     * Date of birth of patient
     */
    private String mPatientBirthday;

    /**
     * Photo image of patient
     */
    private byte[] mPatientPhotoResourceId;

    /**
     * Constant value that represents no image was provided for this word
     */
    private static final byte[] NO_IMAGE_PROVIDED = null;

    /**
     * Create a new Patient object.
     */
    public Patient(long uniquePatientId, String patientName, String patientSurname,
                   String patientGender, String patientBirthday, byte[] patientPhotoResourceId) {
        mUniquePatientId = uniquePatientId;
        mPatientName = patientName;
        mPatientSurname = patientSurname;
        mPatientGender = patientGender;
        mPatientBirthday = patientBirthday;
        mPatientPhotoResourceId = patientPhotoResourceId;
    }

    /**
     * Return Patient unique id
     */
    public long getUniquePatientId() {
        return mUniquePatientId;
    }

    /**
     * Set Patient unique id
     */
    public void setUniquePatientId(long uniquePatientId) {
        this.mUniquePatientId = uniquePatientId;
    }

    /**
     * Return Patient name
     */
    public String getPatientName() {
        return mPatientName;
    }

    /**
     * Set Patient name
     */
    public void setPatientName(String patientName) {
        this.mPatientName = patientName;
    }

    /**
     * Return Patient last name
     */
    public String getPatientSurname() {
        return mPatientSurname;
    }

    /**
     * Set Patient last name
     */
    public void setPatientSurname(String patientSurname) {
        this.mPatientSurname = patientSurname;
    }

    /**
     * Get Patient sex
     */
    public String getPatientGender() {
        return mPatientGender;
    }

    /**
     * Set Patient sex
     */
    public void setPatientGender(String patientGender) {
        this.mPatientGender = patientGender;
    }

    /**
     * Get Patient dateof birth
     */
    public String getPatientBirthday() {
        return mPatientBirthday;
    }

    /**
     * Set Patient date of birth
     */
    public void setPatientBirthday(String patientBirthday) {
        this.mPatientBirthday = patientBirthday;
    }

    /**
     * Get Patient Image/photo id
     */
    public byte[] getPatientPhotoResourceId() {
        return mPatientPhotoResourceId;
    }

    /**
     * Set Patient Image/photo id
     */
    public void setPatientPhotoResourceId(byte[] patientPhotoResourceId) {
        this.mPatientPhotoResourceId = patientPhotoResourceId;
    }

    /**
     * Returns whether or not there is an image for this word.
     */
    public boolean hasImage() {
        return mPatientPhotoResourceId != NO_IMAGE_PROVIDED;
    }

}