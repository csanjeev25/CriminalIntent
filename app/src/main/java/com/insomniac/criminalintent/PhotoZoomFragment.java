package com.insomniac.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;
import android.support.v4.app.DialogFragment;
import java.util.UUID;



public class PhotoZoomFragment extends DialogFragment {

    private File mCrimePhotoFile;
    private ImageView mCrimePhoto;
    private int mLastCrimePhotoHeight = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID uuid = (UUID) getArguments().getSerializable(CRIME_ID);
        Crime crime = CrimeLab.get(getActivity()).getCrime(uuid);
        mCrimePhotoFile = CrimeLab.get(getActivity()).getPhotoFile(crime);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.zoom_layout, null);
        mCrimePhoto = (ImageView) view.findViewById(R.id.crime_photo);

        ViewTreeObserver viewTreeObserver = mCrimePhoto.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhoto();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    private void updatePhoto() {
        int width = mCrimePhoto.getWidth();
        int height = mCrimePhoto.getHeight();
        if(height != mLastCrimePhotoHeight) {
            mLastCrimePhotoHeight = height;
            Bitmap bitmap = PicturUtils.getScaledBitmap(mCrimePhotoFile.getAbsolutePath(), width, height);
            mCrimePhoto.setImageBitmap(bitmap);
        }
    }

    private static final String CRIME_ID = "CRIME_ID";
    public static PhotoZoomFragment newInstance(UUID uuid) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CRIME_ID, uuid);
        PhotoZoomFragment photoFragment = new PhotoZoomFragment();
        photoFragment.setArguments(bundle);
        return photoFragment;
    }
}

