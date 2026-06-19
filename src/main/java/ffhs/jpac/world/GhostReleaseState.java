package ffhs.jpac.world;

/**
 * Beschreibt die Freigabephase eines Geistes im Geisterhaus.
 */
public enum GhostReleaseState {
    /** Der Geist wartet unbeweglich auf das Ende seiner Verzögerung. */
    WAITING_IN_HOUSE,
    /** Der Geist folgt dem kürzesten Weg zum Ausgang. */
    LEAVING_HOUSE,
    /** Der Geist verwendet seine reguläre Persönlichkeitsstrategie. */
    ACTIVE
}
