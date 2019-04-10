package fr.eq3.nose.spot.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.ImageItem;

public class ProgressiveImageLoader {

    private static final int IMAGE_SIZE = 128;

    private final long spotId;
    private final Context context;
    private final Runnable postExecute;
    private List<Integer> ids = null;

    private int cursor = 0;

    ProgressiveImageLoader(Context context, long spotId, Runnable postExecute) {
        this.spotId = spotId;
        this.context = context;
        this.postExecute = postExecute;
    }

    Collection<ImageItem> getNextElements(int nbElements) {
        final ArrayList<ImageItem> imageLoaded = new ArrayList<>();
        if(ids == null)
            this.ids = new DatabaseRequest(this.context).getItemsIdsOfSpot(this.spotId);
        int i;
        for(i = cursor; i - cursor <nbElements && i < ids.size(); ++i){
            ImageItem imageItem = new DatabaseRequest(this.context).getImage(-1);
            imageLoaded.add(imageItem);
            Loader loader = new Loader();
            loader.execute(new Pair<>(i, imageItem));
        }
        cursor = i;
        return imageLoaded;
    }

//    private boolean wait_threads(Set<Future> threads){
//        for(Future t : threads){
//            try {
//                t.get();
//            } catch (InterruptedException e) {
//                Log.i("DIM", "Error : loading interrupted. " + e.getMessage());
//                return false;
//            } catch (ExecutionException e){
//                Log.i("DIM", "Error : execution exception when loading image. " + e.getMessage());
//                return false;
//            }
//        }
//        return true;
//    }

    private class Loader extends AsyncTask<Pair<Integer, ImageItem>, Void, Void> {

        @Override
        protected Void doInBackground(Pair<Integer, ImageItem>... pairs) {
            for(Pair<Integer, ImageItem> pair : pairs){
                final int ressourcesId = pair.first;
                final ImageItem imageItem = pair.second;
                ImageItem img = new DatabaseRequest(ProgressiveImageLoader.this.context).getImage(ressourcesId);
                imageItem.setImage(img.getImage());
                imageItem.setTitle(img.getTitle());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            postExecute.run();
        }
    }
}
