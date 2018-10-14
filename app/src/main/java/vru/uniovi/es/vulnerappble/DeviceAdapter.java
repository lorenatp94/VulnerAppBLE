package vru.uniovi.es.vulnerappble;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends ArrayAdapter<Device> {
    private ArrayList<Device> deviceList;
    private int Resource;
    private AppCompatActivity app;

    DeviceAdapter(AppCompatActivity context, int resource, ArrayList<Device> list) {
        super(context, resource, list );
        Resource=resource;
        app= context;
        deviceList=list;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) app.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View item= inflater.inflate(R.layout.list_item, null);

        TextView textView1 = (TextView)item.findViewById(R.id.device_name);
        textView1.setText(deviceList.get(position).getDeviceName());

        TextView textView2 = (TextView)item.findViewById(R.id.device_address);
        textView2.setText(deviceList.get(position).getDeviceAddress());

        TextView textView3 = (TextView)item.findViewById(R.id.device_rssi);
        textView3.setText(Integer.toString(deviceList.get(position).getRssi()));

        ImageView imageDevice = (ImageView)item.findViewById(R.id.device_image);
        String  user= deviceList.get(position).getuType();

        switch(user){
            case ("1"):
                imageDevice.setImageResource(R.drawable.ic_directions_bike_black_24dp);
                imageDevice.setBackgroundResource(R.drawable.cerclebackgroundpurple);
                break;
            case ("2"):
                imageDevice.setImageResource(R.drawable.ic_directions_car_black_24dp);
                imageDevice.setBackgroundResource(R.drawable.cerclebackgroundgreen);
                break;
            case("3"):
                imageDevice.setImageResource(R.drawable.ic_directions_walk_black_24dp);
                imageDevice.setBackgroundResource(R.drawable.cerclebackgroundblue);
                break;
            default:
                System.out.println("Error");
        }

        return(item);

    }
}
