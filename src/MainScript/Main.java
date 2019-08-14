package MainScript;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import static org.rspeer.runetek.api.commons.Time.sleepUntil;
import static org.rspeer.runetek.api.commons.Time.sleepUntilForDuration;

@ScriptMeta(name = "MainScript",  desc = "manages bot", developer = "TbpT", category = ScriptCategory.MONEY_MAKING)
public class Main extends Script {
    private static final Area BANK_AREA = Area.rectangular(3092, 3241, 3095, 3245);
    private static final Area COW_AREA = Area.rectangular(3194, 3299, 3209, 3285);
    private static final Area POTATO_AREA = Area.rectangular(3141, 3289, 3155, 3279);

    private enum State {HIDE_GRAB,WOODCUT,MINING,FISHING,COOKING,QUESTS,LOW_LIFE}
    private enum SubState {GO_BANK,GO_COW,GO_POTATO,HIDE_HANDLER,FOOD_HANDLER}

    private State state;
    private SubState subState, predictedState;

    Timer timer;

    private int N_logs4Fire = 0; //Number of logs to get before starting firemaking

    private static final Predicate<Item> BONES_PREDICATE = item -> item.getName().contains("Bones");

    @Override
    public void onStart() {
        Log.info("Bot has been initialized!");

        if(Random.nextBoolean())
            Reminder(Random.nextInt(40,60));
        else
            Reminder(Random.nextInt(100,140));

        state = State.HIDE_GRAB;
        if(state == State.HIDE_GRAB)
            subState = SubState.GO_COW;

    }

    public void Reminder(int minutes) {
        timer = new Timer();
        timer.schedule(new RemindTask(), minutes * 60000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            System.out.println("Time's up!");
            onGoodbye();
            timer.cancel(); //Terminate the timer thread
        }
    }

    @Override
    public int loop() {
        switch (state){
            case HIDE_GRAB:
                onHideGrab();
                break;

        }
        return Random.nextInt(1000,2000);
    }

    @Override
    public void onStop() {
        Log.severe("Y U DO DIS!");
    }

    private void onHideGrab(){
        switch (subState){
            case GO_BANK:
                if(BANK_AREA.contains(Players.getLocal())){
                    onBank();
                    subState = predictedState;
                    break;
                }
                else
                    walk(BANK_AREA);
                break;
            case GO_COW:
                if(COW_AREA.contains(Players.getLocal())){
                    subState = SubState.HIDE_HANDLER;
                    break;
                }
                else
                    walk(COW_AREA);
                break;
            case HIDE_HANDLER:
                onHideHandler();
                break;
            case GO_POTATO:
                if(POTATO_AREA.contains(Players.getLocal())){
                    subState = SubState.FOOD_HANDLER;
                    break;
                }
                else
                    walk(POTATO_AREA);
                break;
            case FOOD_HANDLER:

                break;
        }

    }

    private void onHideHandler(){
        if(Players.getLocal().getTarget() != null){
            if(lifecheck()){
                state = State.LOW_LIFE;
                onLowLife();
            }
            else{
                sleepUntilForDuration(() -> !Players.getLocal().isAnimating(),Random.nextInt(1500,2000), 25, Random.nextInt(5000, 7000));
            }
        }
        else {
            if (Inventory.isFull()) {
                if (Inventory.contains("Bones")) {
                    Item item[] = Inventory.getItems(BONES_PREDICATE);
                    int n_bones = item.length - 1;
                    item[Random.nextInt(n_bones)].interact("Bury");
                    sleepUntil(() -> !Players.getLocal().isAnimating(), 25, Random.nextInt(5000, 7000));
                } else {
                    subState = SubState.GO_BANK;
                    predictedState = SubState.GO_COW;
                }
            } else {
                Pickable cowhide = Pickables.getNearest("Cowhide");
                if (cowhide != null) {
                    cowhide.interact("Take");
                    sleepUntil(() -> !Players.getLocal().isMoving() || cowhide.containsAction("Take"), 25, Random.nextInt(5000, 7000));
                } else {
                    Npc cow = Npcs.getNearest(c -> c != null && (c.getName().equals("Cow") || c.getName().equals("Cow calf")) && !c.isHealthBarVisible());
                    if (cow != null) {
                        cow.interact("Attack");
                    }
                }
            }
        }
    }

    private boolean lifecheck(){ //TODO: needs to be checked maybe not getHealthPercent()
        if (Players.getLocal().getHealthPercent() <= 20) {
            Log.severe(Players.getLocal().getHealthPercent());
            return true;
        }
        else {
            Log.fine(Players.getLocal().getHealthPercent());
            return false;
        }
    }

    private void onLowLife(){
        if(Inventory.getCount() > 18){
            subState = SubState.GO_BANK;
            predictedState = SubState.GO_POTATO;
        }
        else
            subState = SubState.GO_POTATO;
    }

    private void onBank(){

    }

    private void onGoodbye(){

    }

    private void walk(Area TargetArea) {
        Player local = Players.getLocal();
        if (sleepUntil(() -> !local.isMoving() || (Movement.getDestinationDistance() < Random.nextInt(3, 6)),25, Random.nextInt(5000, 7000))) {
            if (!TargetArea.contains(local)) {
                if (Movement.getRunEnergy() > Random.nextInt(8, 15) && !Movement.isRunEnabled())
                    Movement.toggleRun(true);
                Movement.walkTo(TargetArea.getCenter().randomize(Random.nextInt(1, 4)));
            }
        }
    }
}
