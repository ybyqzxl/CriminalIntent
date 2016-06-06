package li.panda.example.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Created by xueli on 2016/5/30.
 */
public class CrimeListFragment extends Fragment {

    private static final int REQUEST_CRIME = 1;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private RecyclerView crimeRecyclerView;
    private CrimeAdapter adapter;

    private boolean isSubtitleVisible;
    private LinearLayout ll_show;
    private Button btn;
    private Callbacks callbacks;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        callbacks = (Callbacks) activity;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (savedInstanceState != null) {
            isSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        crimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        crimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        btn = (Button) view.findViewById(R.id.btn_add);
        ll_show = (LinearLayout) view.findViewById(R.id.ll_show);
        updateUI();
        return view;

    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.getCrimeLab(getActivity());
        List<Crime> crimes = crimeLab.getCrimeList();
        if (crimes.size() == 0) {
            crimeRecyclerView.setVisibility(View.GONE);
            ll_show.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Crime crime = new Crime();
                    CrimeLab.getCrimeLab(getActivity()).addCrime(crime);
                    Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                    startActivity(intent);
                }
            });
        } else {
            crimeRecyclerView.setVisibility(View.VISIBLE);
            ll_show.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new CrimeAdapter(crimes);
                crimeRecyclerView.setAdapter(adapter);
            } else {
                adapter.setCrimes(crimes);
                adapter.notifyDataSetChanged();
            }
            updateSubtitle();
        }


    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Crime crimeinHolder;
        private TextView titleTextView;
        private TextView titleDate;
        private CheckBox crimeSolvedCheckBox;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            titleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_textview);
            titleDate = (TextView) itemView.findViewById(R.id.list_item_crime_date_textview);
            crimeSolvedCheckBox = (CheckBox) itemView.findViewById(R.id
                    .list_item_crime_solved_checkbox);
        }

        public void bindCrime(Crime crime) {

            crimeinHolder = crime;
            titleTextView.setText(crimeinHolder.getTitle());
            titleDate.setText(dateFormat.format(crimeinHolder.getDate()));
            crimeSolvedCheckBox.setChecked(crimeinHolder.isSolved());
        }

        @Override
        public void onClick(View v) {
            callbacks.onCrimeSelected(crimeinHolder);
//            Intent intent = CrimePagerActivity.newIntent(getActivity(), crimeinHolder.getId());
//            startActivity(intent);
            // startActivityForResult(intent, REQUEST_CRIME);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> crimeList;

        public CrimeAdapter(List<Crime> crimeList) {
            this.crimeList = crimeList;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = crimeList.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return crimeList.size();
        }

        public void setCrimes(List<Crime> crimes) {
            crimeList = crimes;
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CRIME) {
//            if (resultCode == Activity.RESULT_OK) {
//                UUID crimeId = (UUID) data.getSerializableExtra("notify");
//                CrimeLab.getCrimeLab(getActivity()).getCrime(crimeId);
//                Crime crime = CrimeLab.getCrimeLab(getActivity()).getCrime(crimeId);
//                adapter.notifyItemChanged(crimes.indexOf(crime));
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (isSubtitleVisible) {
            subtitleItem.setTitle("hide_subtitle");
        } else {
            subtitleItem.setTitle("show_subtitle");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crome:
                Crime crime = new Crime();
                CrimeLab.getCrimeLab(getActivity()).addCrime(crime);
//                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
//                startActivity(intent);
                updateUI();
                callbacks.onCrimeSelected(crime);
                return true;
            case R.id.menu_item_show_subtitle:
                isSubtitleVisible = !isSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.getCrimeLab(getActivity());
        int crimeCount = crimeLab.getCrimeList().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);
        if (!isSubtitleVisible) {
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, isSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }
}
