package com.rexlite.rexlitebasicnew;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AirFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AirFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AirFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnNavFrag1;
    private Button btnNavFrag2;
    private Button btnNavFrag3;
    private  Button btnNavFrag4;

    private  Button btnNavFrag5;
    private String deviceId = "13";
    private List<Device> devices;
    private OnFragmentInteractionListener mListener;

    //設定GridLayoutManager Item之間的設定參數
    int spanCount = 3; // columns
    int spacing = 10; // px
    boolean includeEdge = false;

    public AirFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AirFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AirFragment newInstance(String param1, String param2) {
        AirFragment fragment = new AirFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air, container, false);

        new Thread(()->{
            List<Device> data = DataBase.getInstance(getActivity()).getDataUao().findDataByDeviceId(deviceId);
            devices = data;
        }).start();

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.air_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        //利用addItemDecoration設定UI上裝置間的距離
        recyclerView.addItemDecoration( new GridSpacingItemDecoration(spanCount,spacing,includeEdge));

        //Adapter
        DeviceAdapter adapter = new DeviceAdapter();
        recyclerView.setAdapter(adapter);

        //標題與功能選單
    /*    TextView title = view.findViewById(R.id.toolbar_title);
        title.setText("MAXAir");
        ImageView leftIcon = view.findViewById(R.id.left_icon);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
                Animatoo.animateSlideRight(getActivity());
            }
        });*/
       /* btnNavFrag1 = (Button) view.findViewById(R.id.max1);
        btnNavFrag2 = (Button) view.findViewById(R.id.max2);
        btnNavFrag3 = (Button) view.findViewById(R.id.max3);
        btnNavFrag4 = (Button) view.findViewById(R.id.scene);
        btnNavFrag5 = (Button) view.findViewById(R.id.air);

        btnNavFrag1.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 1", Toast.LENGTH_SHORT).show();
                ((Device3Activity) getActivity()).setViewPager(0);

            }

        });

        btnNavFrag2.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                //  Toast.makeText(getActivity(), "Going to Fragment 2", Toast.LENGTH_SHORT).show();
                ((Device3Activity) getActivity()).setViewPager(1);

            }

        });

        btnNavFrag3.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                ((Device3Activity) getActivity()).setViewPager(2);

            }

        });
        btnNavFrag4.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                ((Device3Activity) getActivity()).setViewPager(3);

            }

        });
        btnNavFrag5.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                // Toast.makeText(getActivity(), "Going to Fragment 3", Toast.LENGTH_SHORT).show();
                ((Device3Activity) getActivity()).setViewPager(4);

            }

        });*/

       /* btnNavSecondActivity.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Toast.makeText(getActivity(), "Going to Fragment btnNavSecondActivity", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), SecondActivtiy.class);
                startActivity(intent);

            }

        });*/

        return view;
    }

    //根據裝置的名稱放入圖片
    private void setupDevice() {

        String[] deviceName = new String[] {"bedroom","livingroom","kitchen","bedroom2"};
        devices.add( new Device(deviceName[0],R.drawable.device_max1));
        devices.add( new Device(deviceName[1],R.drawable.device_max1));
        devices.add( new Device(deviceName[2],R.drawable.device_max1));
        devices.add( new Device(deviceName[3],R.drawable.device_max1));
        /*for(int i = 0; i< devices.size() ; i++){
            devices.add( new Device(deviceName[i],R.drawable.device_max1));
        }*/
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

  /*  @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class DeviceAdapter extends  RecyclerView.Adapter<DeviceAdapter.DeviceHolder>{
        /*private List<Device> myData;
        private Fragment fragment;*/

        @NonNull
        @Override
        public DeviceAdapter.DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.show_device,parent,false);
            return new DeviceAdapter.DeviceHolder(view);
        }
       /* public DeviceAdapter(Fragment fragment, List<Device> myData) {
            this.fragment = fragment;
            this.myData = myData;
        }*/

        /**更新資料*/
      /*  public void refreshView() {
            new Thread(()->{
                List<Device> data = DataBase.getInstance(getActivity()).getDataUao().displayAll();
                this.myData = data;
                getActivity().runOnUiThread(() -> {
                    notifyDataSetChanged();
                });
            }).start();
        }*/

        @Override
        public void onBindViewHolder(@NonNull AirFragment.DeviceAdapter.DeviceHolder holder, int position) {
            final Device device = devices.get(position);
            holder.nameText.setText(device.getDeviceName());
            holder.deviceImage.setImageResource(device.getDeviceIcon());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClicked(device);
                }
            });
        }

        @Override
        public int getItemCount() {
            if(devices==null){
                return 0;
            } else {
                return devices.size();
            }
        }

        public class DeviceHolder extends RecyclerView.ViewHolder{
            ImageView deviceImage;
            TextView nameText;
            public DeviceHolder(@NonNull View itemView) {
                super(itemView);
                deviceImage = itemView.findViewById(R.id.device_icon);
                nameText = itemView.findViewById(R.id.device_name);
            }
        }
    }
    //點擊個別裝置
    private void itemClicked(Device device) {
        Intent intent = new Intent(getActivity(),Max1SettingActivity.class);

      /*  if(deviceId=="14"){
            maxSceneSN = UDP.hexToByte(device.getDeviceSN());
            Bundle sendInfo = new Bundle();
            sendInfo.putByteArray("deviceSN",maxSceneSN);
            sendInfo.putString("deviceName",device.getDeviceName());
            intent.putExtras(sendInfo);
            startActivity(intent);
        }
        if(deviceId=="16"){
            title.setText("MAXLiTE2");

        }
        if(deviceId=="18"){
            title.setText("MAXLiTE3");

        }
        if(deviceId=="0b"){
            title.setText("MAXScene");

        }*/
        startActivity(intent);
        Animatoo.animateSlideLeft(getActivity());
    }
}
