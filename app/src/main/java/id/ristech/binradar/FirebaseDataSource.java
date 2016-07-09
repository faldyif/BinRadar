package id.ristech.binradar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Faldy on 7/9/2016.
 */
public class FirebaseDataSource extends DataSource {

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;
    public DatabaseReference databaseReference;
    public List<Marker> markersCache = new ArrayList<Marker>();
    public List<Trash> trashes = new ArrayList<Trash>();

    private static Bitmap ICON_LARGE = null;
    private static Bitmap ICON_MEDIUM = null;
    private static Bitmap ICON_SMALL = null;

    public FirebaseDataSource(Resources res) {
        if (res == null) throw new NullPointerException();

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

        ICON_LARGE = BitmapFactory.decodeResource(res, R.drawable.pinbesar);
        ICON_MEDIUM = BitmapFactory.decodeResource(res, R.drawable.pinsedang);
        ICON_SMALL = BitmapFactory.decodeResource(res, R.drawable.pinkecil);
    }

    /**
     * This method get the Markers if they have already been downloaded once.
     *
     * @return List of Marker objects or NULL if not downloaded yet.
     */
    public List<Marker> getMarkers() {
        return markersCache;
    }

     public void loadFirebase() {

         databaseReference = FirebaseDatabase.getInstance().getReference("trashes");
         databaseReference.keepSynced(false);

         databaseReference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 for(DataSnapshot trashSnapshot : dataSnapshot.getChildren()) {
                     final List<Marker> markerList = new ArrayList<Marker>();
                     Trash newTrash = trashSnapshot.getValue(Trash.class);

                     Double latitude = newTrash.getLatitude();
                     Double longitude = newTrash.getLongitude();
                     String description = newTrash.getDescription();
                     Integer type = newTrash.getType();
                     Bitmap pinImage = null;
                     if(type == R.id.trash_small) {
                         pinImage = ICON_SMALL;
                     } else if(type == R.id.trash_medium) {
                         pinImage = ICON_MEDIUM;
                     } else {
                         pinImage = ICON_LARGE;
                     }

                     Marker tempMarker = new IconMarker(description, latitude, longitude, 0, Color.RED, pinImage);

                     markerList.add(tempMarker);
                     markersCache.add(tempMarker);

                     ARData.addMarkers(markerList);
                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });
    }
}
