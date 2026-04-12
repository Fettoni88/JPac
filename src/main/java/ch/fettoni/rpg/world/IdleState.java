package ch.fettoni.rpg.world;

public class IdleState implements NPCState {

    @Override
    public void update(NPC npc, World world, double deltaTime) {

        npc.decreaseTimer(deltaTime);

        if (npc.isTimerFinished()) {
            npc.chooseRandomDirection();
            npc.resetTimer();
        }

        npc.move(world, deltaTime);
    }
}