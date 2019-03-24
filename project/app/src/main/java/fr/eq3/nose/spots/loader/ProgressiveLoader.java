package fr.eq3.nose.spots.loader;

import java.util.Collection;

public interface ProgressiveLoader<E> {
    Collection<E> getNextElements(int nbElements, boolean isNeededToWait);
    void reset();
}
