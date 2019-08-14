package MainScript;

import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
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

import java.util.function.Predicate;

import static org.rspeer.runetek.api.commons.Time.sleepUntil;

@ScriptMeta(name = "MainScript",  desc = "manages bot", developer = "TbpT", category = ScriptCategory.MONEY_MAKING)
public class Main extends Script {
    private static final Area BANK_AREA = Area.rectangular(3092, 3241, 3095, 3245);
    private static final Area COW_AREA = Area.rectangular(3194, 3299, 3209, 3285);
    private static final Area POTATO_AREA = Area.rectangular(3141, 3289, 3155, 3279);

    private enum State {HIDE_GRAB,WOODCUT,MINING,FISHING,COOKING,QUESTS,LOW_LIFE}
    private enum SubState {GO_BANK,GO_COW,GO_POTATO,HIDE_HANDLER,FOOD_HANDLER}

    private State state;
    private SubState subState, predictedState;

    private int N_logs4Fire = 0; //Number of logs to get before starting firemaking

    private static final Predicate<Item> BONES_PREDICATE = item -> item.getName().contains("Bones");

    @Override
    public void onStart() {
        Log.info("Bot has been initialized!");
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
                    sleepUntil(() -> !Players.getLocal().isAnimating() || cowhide.containsAction("Take"), 25, Random.nextInt(5000, 7000));
                } else {
                    Npc cow = Npcs.getNearest(c -> c != null && (c.getName().equals("Cow") || c.getName().equals("Cow calf")) && !c.isHealthBarVisible());
                    if (cow != null) {
                        cow.interact("Attack");
                    }
                }
            }
        }
    }

    private boolean lifecheck(){ //TODO: needs to be checked
        if(Players.getLocal().getHealthPercent() < 10){
            return true;
        }
        else
            return false;
    }

    private void onLowLife(){
        if(Inventory.getCount() > 20){
            subState = SubState.GO_BANK;
            predictedState = SubState.GO_POTATO;
        }
    }

    private void onBank(){

    }

    private void walk(Area TargetArea) {
        Player local = Players.getLocal();
        if (sleepUntil(() -> !local.isMoving() || Movement.getDestinationDistance() < Random.nextInt(3, 6),25, Random.nextInt(5000, 7000))){
            if (!TargetArea.contains(local)){
                if(Movement.getRunEnergy() > Random.nextInt(8, 15) && !Movement.isRunEnabled())
                    Movement.toggleRun(true);
                Movement.walkTo(TargetArea.getCenter().randomize(Random.nextInt(1, 4)));
            }
        }
    }
}
