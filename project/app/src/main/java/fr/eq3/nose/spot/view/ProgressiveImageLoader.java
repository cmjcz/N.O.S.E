package fr.eq3.nose.spot.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

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
    private final ExecutorService threads = Executors.newCachedThreadPool();
    private List<Integer> ids = null;

    private int cursor = 0;

    ProgressiveImageLoader(long spotId, Context context) {
        this.spotId = spotId;
        this.context = context;
    }

    Collection<ImageItem> getNextElements(int nbElements, boolean isNeededToWait) {
        final HashSet<Future> threads = new HashSet<>();
        final ArrayList<ImageItem> imageLoaded = new ArrayList<>();
        if(ids == null)
            this.ids = new DatabaseRequest(this.context).getItemsIdsOfSpot(this.spotId);
        int i;
        for(i = cursor; i - cursor <nbElements && i < ids.size(); ++i){
            ImageItem imageItem = new DatabaseRequest(this.context).getImage(-1);
            imageLoaded.add(imageItem);
            Loader loader = new Loader(ids.get(i), imageItem);
            Future thread = this.threads.submit(loader);
            threads.add(thread);
        }
        cursor = i;
        if(isNeededToWait){
            if(!wait_threads(threads))
                return null;
        }
        return imageLoaded;
    }

    private boolean wait_threads(Set<Future> threads){
        for(Future t : threads){
            try {
                t.get();
            } catch (InterruptedException e) {
                Log.i("DIM", "Error : loading interrupted. " + e.getMessage());
                return false;
            } catch (ExecutionException e){
                Log.i("DIM", "Error : execution exception when loading image. " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private class Loader implements Runnable{
        private final int ressourcesId;
        private final ImageItem imageItem;

        Loader(int ressourceId, ImageItem imageItem) {
            this.ressourcesId = ressourceId;
            this.imageItem = imageItem;
        }

        @Override
        public void run() {
            ImageItem img = new DatabaseRequest(ProgressiveImageLoader.this.context).getImage(ressourcesId);
            int dim = Math.max(img.getImage().getWidth(), img.getImage().getHeight());
            dim /= IMAGE_SIZE;
            Bitmap bitmap = Bitmap.createScaledBitmap(img.getImage(), img.getImage().getWidth() / dim, img.getImage().getHeight() / dim, false);
            imageItem.setImage(bitmap);
            imageItem.setTitle(img.getTitle());
        }
    }
}
