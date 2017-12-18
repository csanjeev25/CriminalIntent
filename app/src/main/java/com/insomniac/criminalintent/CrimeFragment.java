package com.insomniac.criminalintent;

/**
 * Created by Sanjeev on 4/2/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.format.DateFormat;

import java.io.File;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private static final String EXTRA_CRIME_ID = "com.insomniac.criminalIntent.CrimeFragment.crimeId_id";
    private static final String HAS_CRIME_CHANGED = "has_changed";
    private static final String ARG_CRIME_ID = "arg_crime_id";
    public boolean isChanged = false;
    private CheckBox mSolvedCheckbox;
    private static final String TAG = "CrimeFragment";
    private static final String crimeID = "com.insomniac.criminalIntent.CrimeFragment.crimeId";
    private static final String dateDialog = "dateDialog";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final String timeDialog = "timeDialog";
    private static final String photoDialog = "photoDialog";
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static int call = 0;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private int mLastCrimePhotoHeight = 0;
    private Callbacks mCallbacks;
    private int requestedPermission = 0;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static CrimeFragment newInstance(UUID crime_id){
        Bundle bundle = new Bundle();
        bundle.putSerializable(crimeID,crime_id);

        //Toast.makeText(context,"The North Remembers",Toast.LENGTH_SHORT).show();
        //.e(crimeID,"The North Remembers");

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(bundle);
        return crimeFragment;
    }

    /*public static boolean hasCrimeChanged(Intent result) {
        return result.getBooleanExtra(HAS_CRIME_CHANGED, false);
    }

    public static UUID getCrimeId(Intent result) {
        return (UUID) result.getSerializableExtra(ARG_CRIME_ID);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.menu_item_delete_crime :
                CrimeLab.get(getActivity()).deleteCrime(mCrime.getId());
                getActivity().finish();
                return true;
            default : return true;
        }
    }


    @Override
    public void onPause(){
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_suspect);
        }else{
            suspect = getString(R.string.crime_report_no_suspect);
        }

        String report = getString(R.string.crime_report,mCrime.getTitle(),dateString,solvedString,suspect);

        return report;

    }

    public void updatePhotoView(){
        if(mPhotoFile != null && mPhotoFile.exists()) {
            int width = mPhotoView.getWidth();
            int height = mPhotoView.getHeight();
            if(height != mLastCrimePhotoHeight) {
                mLastCrimePhotoHeight = height;
                Bitmap bitmap = PicturUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
                mPhotoView.setImageBitmap(bitmap);
               mPhotoView.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        } else {
           mPhotoView.setImageBitmap(null);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu,menuInflater);
        menuInflater.inflate(R.menu.fragment_crime_delete,menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(crimeID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

  /* public Intent returnResult() {
        Log.d(TAG,"returnResult");
        Intent data = new Intent();
        data.putExtra(HAS_CRIME_CHANGED,isChanged);
        data.putExtra(ARG_CRIME_ID,crimeID);
        return data;
    }*/

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            Log.d(TAG, "onActivityResult");
            mCrime.setDate(date);
            updateDate();
            updateCrime();
            updateTime();
        } else if (requestCode == REQUEST_TIME) {
            int hour = data.getIntExtra(TimePickerFragment.sExtraHour, -1);
            int minute = data.getIntExtra(TimePickerFragment.sExtraMinute, -1);
            if (hour > 0 && minute > 0) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(mCrime.getDate());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                mCrime.setDate(calendar.getTime());
                updateTime();
                updateCrime();
                updateDate();
            }} else if (requestCode == REQUEST_CONTACT && data != null) {
                Uri contactUri = data.getData();

                String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

                Cursor cursor = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

                try {
                    if (cursor.getCount() == 0)
                        return;
                    cursor.moveToFirst();
                    String suspect = cursor.getString(0);
                    //Toast.makeText(getActivity(),suspect,Toast.LENGTH_SHORT).show();
                    mCrime.setSuspect(suspect);
                    //Toast.makeText(getActivity(),mCrime.getSuspect(),Toast.LENGTH_SHORT).show();
                    mSuspectButton.setText(suspect);
                    updateCrime();
                    if(call == 1){
                        callSuspect();
                    }
                } finally {
                    cursor.close();
                }
            }else if(requestCode == REQUEST_PHOTO)
                updateCrime();
                updatePhotoView();
        }


    private void updateTime(){
        mTimeButton.setText(DateFormat.format("HH:mm:ss z", mCrime.getDate()));
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.format("EEEE, MMM d, yyyy", mCrime.getDate()));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                isChanged = true;
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setEnabled(true);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentManager manager = getFragmentManager();
                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(mCrime.getDate());
                datePickerFragment.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                datePickerFragment.show(manager,dateDialog);

            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"mTimeButtonClick");
                android.support.v4.app.FragmentManager manager = getFragmentManager();
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mCrime.getDate());
                timePickerFragment.setTargetFragment(CrimeFragment.this,REQUEST_TIME);
                timePickerFragment.show(manager,timeDialog);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.camera_view);

        ViewTreeObserver viewTreeObserver = mPhotoView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View view, View view1) {
                updatePhotoView();
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                PhotoZoomFragment photoZoomFragment = PhotoZoomFragment.newInstance(mCrime.getId());;
                photoZoomFragment.show(fragmentManager,photoDialog);
            }
        });

        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                isChanged = true;
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report_send);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject));
                intent.createChooser(intent,getString(R.string.send_report));
                startActivity(intent);*/

                String mimeType = "text/plain";
                String report = getCrimeReport();
                String crime_subject = getString(R.string.crime_report_subject);

                Intent messageIntent = ShareCompat.IntentBuilder.from(getActivity()).setSubject(crime_subject).setType(mimeType).setText(report).getIntent();
                startActivity(messageIntent);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect_button);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivityForResult(pickContact,REQUEST_CONTACT);
                chooseSuspect();
            }
        });

        if(mCrime.getSuspect() != null)
            mSuspectButton.setText(mCrime.getSuspect());

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        mCallButton = (Button) v.findViewById(R.id.crime_call_button);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCrime.getSuspect() != null){
                    callSuspect();
                }else{
                    call = 1;
                    chooseSuspect();
                }
            }
        });

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = (mPhotoFile != null) && (captureImage.resolveActivity(packageManager) != null);

        if(canTakePhoto){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                captureImage.putExtra(MediaStore.ACTION_IMAGE_CAPTURE,mPhotoFile);
            }else {
                File file = new File(mPhotoFile.getPath());
                Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(),getActivity().getApplicationContext().getPackageName() + ".provider",file);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            }
            captureImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoView = (ImageView) v.findViewById(R.id.camera_view);

        return v;
    }

    public void chooseSuspect(){
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContact,REQUEST_CONTACT);
    }


    public void callSuspect(){
        //Toast.makeText(getActivity(),"call Suspect0",Toast.LENGTH_SHORT).show();
        //Toast.makeText(getActivity(),"callSuspect1:",Toast.LENGTH_SHORT).show();
        String[] contactsQueryFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };
        Cursor contactCursor = getActivity().getContentResolver().
                query(ContactsContract.Contacts.CONTENT_URI,
                        contactsQueryFields,
                        ContactsContract.Contacts.DISPLAY_NAME + " = '" + mCrime.getSuspect() + "'",
                        null, null);
        //Toast.makeText(getActivity(),ContactsContract.Contacts.DISPLAY_NAME + " = '" + mCrime.getSuspect() + "'",Toast.LENGTH_SHORT).show();
        try {
            //Toast.makeText(getActivity(),"callSuspect2",Toast.LENGTH_SHORT).show();
            if (contactCursor.getCount() == 0) {
                //Toast.makeText(getActivity(),"callSuspect3",Toast.LENGTH_SHORT).show();
                return;
            }
            int hasPhone;
            //Toast.makeText(getActivity(),contactCursor.getCount(),Toast.LENGTH_SHORT).show();
            contactCursor.moveToFirst();
            int _id = contactCursor.getInt(0);
            hasPhone = contactCursor.getInt(1);
            //Toast.makeText(getActivity(),hasPhone,Toast.LENGTH_SHORT).show();
            if (hasPhone > 0) {
                //Toast.makeText(getActivity(),"callSuspect4",Toast.LENGTH_SHORT).show();
                String[] phoneQueryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor phoneCursor =getActivity().getContentResolver().
                        query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                phoneQueryFields,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '" + _id + "'",
                                null, null);
                try {
                    //Toast.makeText(getActivity(),"callSuspect4",Toast.LENGTH_SHORT).show();
                    if (phoneCursor.getCount() == 0) {
                        return;
                    }
                    phoneCursor.moveToFirst();
                    //Toast.makeText(getActivity(),"callSuspect5",Toast.LENGTH_SHORT).show();
                    String phoneNumber = phoneCursor.getString(0);
                    Uri uri = Uri.parse("tel:" + phoneNumber);
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(callIntent);
                } finally {
                    //Toast.makeText(getActivity(),"callSuspect6",Toast.LENGTH_SHORT).show();
                    phoneCursor.close();
                }
            }
        } finally {
            //Toast.makeText(getActivity(),"callSuspect7",Toast.LENGTH_SHORT).show();
            contactCursor.close();
        }
    }




}
