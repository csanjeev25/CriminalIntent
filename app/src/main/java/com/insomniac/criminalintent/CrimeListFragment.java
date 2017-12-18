package com.insomniac.criminalintent;

/**
 * Created by Sanjeev on 4/2/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    //private int poistionChanged;
    //private UUID isChangedId;
    boolean isChanged;
    private static final String TAG = "CrimeListFragment";
    private static final String HAS_CRIME_CHANGED = "has_changed";
    private static final String ARG_CRIME_ID = "arg_crime_id";
    private static int REQUEST_CRIME = 1;
    public CrimeHolder dataChanged;
    private static final String SAVED_POSITION = "saved_position";
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.menu_item_new_crime : Crime crime = new Crime();
                                            CrimeLab.get(getActivity()).addCrime(crime);
                                            updateUI();
                                            mCallbacks.onCrimeSelected(crime);
                                            return true;
            case R.id.menu_item_show_subtitle : mSubtitleVisible = !mSubtitleVisible;
                                                getActivity().invalidateOptionsMenu();
                                                updateSubtitle();
                                                return true;
            default : return super.onOptionsItemSelected(menuItem);
        }
    }



    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisible)
            subtitleItem.setTitle(R.string.hide_subtitle);
        else
            subtitleItem.setTitle(R.string.show_subtitle);

    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        if(!mSubtitleVisible)
            subtitle = null;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = (RecyclerView) view
                .findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }


        updateUI();

        return view;
    }



    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            /*if (isChanged) {
                int mItemChangedPosition = mAdapter.getCrimeIndex(isChangedId);
                Log.d(TAG, "Changed position :" + mItemChangedPosition);
                mAdapter.notifyItemChanged(mItemChangedPosition);
            }*/
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private TextView mTimeTextView;
        private View mView;


        private Crime mCrime;

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_crime_time_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_data_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_item);
        }

        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
        }

        @Override
        public void onClick(View v) {
            Log.e(TAG,"Valar Morghulis");
            //Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getId());
            //Toast.makeText(getActivity(),"Valar Morghulis",Toast.LENGTH_SHORT).show();
            //startActivityForResult(intent,REQUEST_CRIME);
            //.makeText(getActivity(),"Valar Morghulis",Toast.LENGTH_SHORT).show();
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == REQUEST_CRIME){
            if(data != null){
                Log.d(TAG,"data is not null");
                /*isChanged = CrimeActivity.hasCrimeChanged(data);
                isChangedId = CrimeActivity.getCrimeId(data);*/
            }
        }
        Log.d(TAG, "onActivityResult: ");
        if (resultCode != Activity.RESULT_OK) {
            isChanged = false;
            Log.d(TAG, "not OK: " + resultCode);
            return;
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
            dataChanged = holder;
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        private int getCrimeIndex(UUID crimeId) {
            for (int i = 0; i < mCrimes.size(); i++) {
                Crime crime = mCrimes.get(i);
                if (crime.getId().equals(crimeId)) {
                    return i;
                }
            }
            return -1;
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle onSavedInstanceState) {
        super.onSaveInstanceState(onSavedInstanceState);
        onSavedInstanceState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

}
