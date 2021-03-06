package com.ArmGuide.tourapplication.ui.myTours;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ArmGuide.tourapplication.Constants;
import com.ArmGuide.tourapplication.R;
import com.ArmGuide.tourapplication.StateViewModel;
import com.ArmGuide.tourapplication.models.Tour;
import com.ArmGuide.tourapplication.ui.createTour.ChooseATravelPackageAdd;
import com.ArmGuide.tourapplication.ui.createTour.CreateTourActivity;
import com.ArmGuide.tourapplication.ui.tours.by.category.ToursByCategoryFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;

public class MyToursFragment extends Fragment implements MyToursRecyclerViewAdapter.OnToursViewHolderCLickListener {


    private boolean TOUR_AGENCY;
    private MyToursViewModel myToursViewModel;
    private StateViewModel stateViewModel;
    private MyToursRecyclerViewAdapter adapter;
    private List<Tour> toursList;

   // views
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView noTours_tv;



    public MyToursFragment(boolean TOUR_AGENCY) {
        this.TOUR_AGENCY = TOUR_AGENCY;
    }

    @SuppressLint("RestrictedApi")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_tours, container, false);

        // view init
        progressBar=root.findViewById(R.id.my_tours_pb);
        recyclerView=root.findViewById(R.id.my_tours_rv);
        noTours_tv=root.findViewById(R.id.my_tours_tv);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        toursList=new ArrayList<>();
        adapter=new MyToursRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnToursViewHolderCLickListener(this);


        myToursViewModel = ViewModelProviders.of(this).get(MyToursViewModel.class);

        if(getActivity()!=null)
        stateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);

        fab=root.findViewById(R.id.my_tours_fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                Intent intent=new Intent(getActivity(), CreateTourActivity.class);
                startActivity(intent);
              }
              else{
                  Toast.makeText(getActivity(),"You must sign in as tour company to create tours",Toast.LENGTH_SHORT).show();
              }
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                                               if(getActivity()!=null){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("DELETE TOUR")
                        .setMessage(R.string.delete_dialog_message)
                        .setIcon(R.drawable.ic_application)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteTour(viewHolder.getAdapterPosition());
                                dialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());

                                dialog.dismiss();
                            }
                        });
                AlertDialog signOutDialog = builder.create();
                signOutDialog.show();

                                               }

            }
        }).attachToRecyclerView(recyclerView);

        return root;
    }

    private void deleteTour(int position) {
        Tour tour=adapter.getTour(position);
        String tourId=tour.getId();
        if(TOUR_AGENCY){
            myToursViewModel.deleteTourFromCompany(tour);
        }else{
            myToursViewModel.deleteTourFromTourist(tourId);
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            noTours_tv.setVisibility(VISIBLE);
            noTours_tv.setText("you aren't sign in");
            fab.setVisibility(VISIBLE);
        }

        if(getActivity()!=null&&FirebaseAuth.getInstance().getCurrentUser()!=null)
        stateViewModel.getState().observe(this, new Observer<Boolean>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean ){
                    TOUR_AGENCY=aBoolean;
                    fab.setVisibility(VISIBLE);
                    getCompanyTours();
                    adapter.setTOUR_AGENCY(aBoolean);
                }else{
                    TOUR_AGENCY=aBoolean;
                    fab.setVisibility(View.GONE);
                    getTouristsTours();
                }
            }
        });

    }

    private void getCompanyTours(){
        progressBar.setVisibility(View.VISIBLE);

        myToursViewModel.getCompanyToursList().observe(this, new Observer<List<Tour>>() {
            @Override
            public void onChanged(List<Tour> tours) {
                progressBar.setVisibility(View.GONE);
                if (tours.size() == 0) {
                    noTours_tv.setVisibility(VISIBLE);
                } else {
                    noTours_tv.setVisibility(View.GONE);
                    toursList.clear();
                    toursList.addAll(tours);
                    adapter.setTours(toursList);
                }
            }
        });
    }
    private void getTouristsTours(){
        progressBar.setVisibility(View.VISIBLE);
        myToursViewModel.getTouristsToursList().observe(this, new Observer<List<Tour>>() {
            @Override
            public void onChanged(List<Tour> tours) {
                progressBar.setVisibility(View.GONE);
                if(tours.size()==0){
                    noTours_tv.setVisibility(VISIBLE);
                }else{
                    noTours_tv.setVisibility(View.GONE);
                    toursList.clear();
                    toursList.addAll(tours);
                    adapter.setTours(toursList);
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            fab.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onToursViewHolderClick(int position) {
        Tour tour=adapter.getTour(position);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container,
                            new MyToursChooseATravelPackageAdd(tour,TOUR_AGENCY)).addToBackStack(null).commit();
        }
    }

    @Override
    public void onEditButtonClick(int position) {
        Tour tour=adapter.getTour(position);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container,
                            new MyToursEditTour(tour)).addToBackStack(null).commit();
        }
    }
}