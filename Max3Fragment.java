package com.rexlite.rexlitebasicnew;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.rexlite.rexlitebasicnew.RoomDataBase.DataBase;
import com.rexlite.rexlitebasicnew.RoomDataBase.Device;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Max3Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Max3Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Max3Fragment extends Fragment {
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
    byte[] deviceSN;//點擊該裝置時傳送對應的SN
    private  Button btnNavFrag5;
    //設定GridLayoutManager Item之間的設定參數
    int spanCount = 3; // columns
    int spacing = 10; // px
    boolean includeEdge = false;
    private List<Device> devices;
    private String deviceId = "18";

    Max3Fragment.DeviceAdapter adapter = new Max3Fragment.DeviceAdapter();

    private OnFragmentInteractionListener mListener;

    public Max3Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Max3Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Max3Fragment newInstance(String param1, String param2) {
        Max3Fragment fragment = new Max3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //fragment真正最一開始的Function
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        View view = inflater.inflate(R.layout.fragment_max3, container, false);
        /**取得max3的資料*/
        new Thread(()->{
            List<Device> data = DataBase.getInstance(getActivity()).getDataUao().findDataByDeviceId(deviceId);
            devices = data;
            //當資料更新時通知畫面避免資料還沒撈取就顯示畫面

            getActivity().runOnUiThread(new Runnable(){
                public void run() {
                    // UI code goes here
                    adapter.notifyDataSetChanged();
                }
            });
            /*adapter.notifyItemInserted(devices.size());
            adapter.notifyDataSetChanged();*/

        }).start();
        //Recycler
        devices = new ArrayList<>();
        //標題與功能選單
        /*TextView title = view.findViewById(R.id.toolbar_title);
        title.setText("MAXLiTE3");
        ImageView leftIcon = view.findViewById(R.id.left_icon);
        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.putExtra("resume",true);
                startActivity(intent);
                Animatoo.animateSlideRight(getActivity());
            }
        });*/
        //根據裝置的名稱放入圖片
        /*String[] deviceName = new String[] {"bedroom","livingroom","kitchen"};
        devices.add( new Device(deviceName[0],R.drawable.device_max3));
        devices.add( new Device(deviceName[1],R.drawable.device_max3));
        devices.add( new Device(deviceName[2],R.drawable.device_max3));*/
        /*for(int i = 0; i< devices.size() ; i++){
            devices.add( new Device(deviceName[i],R.drawable.device_max1));
        }*/
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.max3_recycler);
        recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        //利用addItemDecoration設定UI上裝置間的距離
        recyclerView.addItemDecoration( new GridSpacingItemDecoration(spanCount,spacing,includeEdge));

        //Adapter
       // Max3Fragment.DeviceAdapter adapter = new Max3Fragment.DeviceAdapter();
        recyclerView.setAdapter(adapter);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

   /* @Override
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public class DeviceAdapter extends  RecyclerView.Adapter<Max3Fragment.DeviceAdapter.DeviceHolder>{
        @NonNull
        @Override
        public Max3Fragment.DeviceAdapter.DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.show_device,parent,false);
            return new Max3Fragment.DeviceAdapter.DeviceHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Max3Fragment.DeviceAdapter.DeviceHolder holder, int position) {
            final Device device = devices.get(position);
            holder.nameText.setText(device.getDeviceName());
            holder.deviceImage.setImageResource(device.getDeviceIcon());
            holder.subtitleText.setText(device.getSubtitle());
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
            TextView subtitleText;
            public DeviceHolder(@NonNull View itemView) {
                super(itemView);
                deviceImage = itemView.findViewById(R.id.device_icon);
                nameText = itemView.findViewById(R.id.device_name);
                subtitleText = itemView.findViewById(R.id.device_subtitle);
            }
        }
    }
    //點擊個別裝置
    private void itemClicked(Device device) {
        Intent intent = new Intent(getActivity(),Max3SettingActivity.class);
        deviceSN = hexToByte(device.getDeviceSN());
        Bundle sendInfo = new Bundle();
        sendInfo.putByteArray("deviceSN", deviceSN);
        sendInfo.putString("deviceName",device.getDeviceName());
        if(device.getSubtitle() != null){
            sendInfo.putString("deviceSubtitle",device.getSubtitle());
        }
        intent.putExtras(sendInfo);
        startActivity(intent);
        Animatoo.animateSlideLeft(getActivity());
    }
    public  byte[] hexToByte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i=0 ; i<bytes.length ; i++)
            bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }
}
