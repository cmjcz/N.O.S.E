package fr.eq3.nose.spot.spot_activity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.eq3.nose.spot.items.DatabaseRequest;
import fr.eq3.nose.spot.items.ImageItem;
import fr.eq3.nose.spot.items.exceptions.ImageNotFoundException;

public final class ProgressiveImageLoader {

    private final long spotId;
    private final Context context;
    private final Runnable postExecute;
    private List<Integer> ressourcesIds = null;

    private int cursor = 0;

    ProgressiveImageLoader(Context context, long spotId, Runnable postExecute) {
        this.spotId = spotId;
        this.context = context;
        this.postExecute = postExecute;
    }

    Collection<ImageItem> getNextElements(int nbElements) {
        final ArrayList<ImageItem> imageLoaded = new ArrayList<>();
        List<Integer> ids = new DatabaseRequest(this.context).getItemsIdsOfSpot(this.spotId);
        if(this.ressourcesIds == null || this.ressourcesIds.size() != ids.size()){
            this.ressourcesIds = ids;
        }
        int i;
        for(i = cursor; i - cursor <nbElements && i < this.ressourcesIds.size(); ++i){
            ImageItem imageItem;
            try {
                imageItem = new DatabaseRequest(this.context).getImage(-1);
                imageLoaded.add(imageItem);
                Loader loader = new Loader();
                loader.execute(new Pair<>(this.ressourcesIds.get(i), imageItem));
            } catch (ImageNotFoundException e) {
                e.printStackTrace();
            }
        }
        cursor = i;
        return imageLoaded;
    }

    private class Loader extends AsyncTask<Pair<Integer, ImageItem>, Void, Void> {

        @Override
        protected Void doInBackground(Pair<Integer, ImageItem>... pairs) {
            for(Pair<Integer, ImageItem> pair : pairs){
                final int ressourcesId = pair.first;
                final ImageItem imageItem = pair.second;
                ImageItem img = null;
                try {
                    img = new DatabaseRequest(ProgressiveImageLoader.this.context).getImage(ressourcesId);
                    imageItem.setData(img.getData());
                    imageItem.setName(img.getName());
                } catch (ImageNotFoundException e) {
                    e.printStackTrace();
                }
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
