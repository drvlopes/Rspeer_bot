package QuestMaker;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.LoginResponseListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.LoginResponseEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import static org.rspeer.runetek.api.commons.Time.sleepUntil;
import static org.rspeer.runetek.api.commons.Time.sleepUntilForDuration;

@ScriptMeta(name = "QuestMaker",  desc = "quest maker converted for rspeer", developer = "TbpT", category = ScriptCategory.OTHER)
public class Main extends Script implements ChatMessageListener, LoginResponseListener {

    private static final Area COOK_AREA = Area.rectangular(3210, 3212, 3205, 3217);
    private static final Area EGG_AREA = Area.rectangular(3226, 3301, 3235, 3295);
    private static final Area COWS_AREA = Area.rectangular(3251, 3276, 3256, 3272);
    private static final Area WHEAT_AREA = Area.rectangular(3162, 3293, 3158, 3297);
    private static final Area MILL_AREA = Area.rectangular(3165, 3305, 3168, 3309);
    private static final Area BANK_AREA = Area.rectangular(3092, 3241, 3095, 3245);
    private static final Area SHEAR_AREA = Area.rectangular(3188, 3274, 3191, 3271);
    private static final Area SHEEP_AREA = Area.rectangular(3212, 3258, 3201, 3269);
    private static final Area WHEEL_AREA = Area.rectangular(3208, 3217, 3213, 3212,1);

    private int currentQuest = 0;
    private boolean run_once = false;

    int[] mundosF2P = new int[] {301, 308, 316, 326, 335, 379, 380, 382, 383, 384, 393, 394, 397, 398, 399, 417, 418, 430, 431, 433, 434, 435, 436, 437, 438, 439, 440, 451, 452, 453, 454, 455, 456, 457, 458, 459, 469, 470, 471, 472, 473, 474, 475, 476, 477, 497, 498, 499, 500, 501, 502, 503, 504};


    @Override
    public void notify(LoginResponseEvent loginResponseEvent) {
        Log.severe(" " + loginResponseEvent.getResponse().toString());
        if(loginResponseEvent.getResponse() == LoginResponseEvent.Response.MEMBERSHIP_REQUIRED){
            setStopping(true);
//            Game.getClient().setLoginWorldSelectorOpen(true);
//            sleepUntil(() -> Game.getClient().isLoginWorldSelectorOpen(), 1000, 5000);
//            Time.sleep(Random.nextInt(1000, 3000));
//            Game.getClient().setWorld(Worlds.get(mundosF2P[Random.nextInt(0, mundosF2P.length-1)]));
//            Game.getClient().setLoginWorldSelectorOpen(false);
//            sleepUntil(() -> !Game.getClient().isLoginWorldSelectorOpen(), 1000, 5000);
//            Time.sleep(Random.nextInt(1000, 3000));
        }
    }

    private enum Cook_state {GO_COOK,TALK_COOK,GO_EGG,GET_EGG,GO_COW,GET_MILK,GO_WHEAT,GET_WHEAT,GO_MILL,UP_MILL,USE_HOPPER,OPERATE_MILL,DOWN_MILL,GET_FLOUR}
    private enum Sheep_state {CHECK,GO_FARMER,GET_SHEAR,TALK_FARMER,GO_SHEEP,GET_WOOL,GO_WHEEL,USE_WHEEL}

    private Cook_state c_state;
    private Sheep_state s_state;

    private void evaluateQuestsState() {
        run_once = true;
        // 0 -> no quest to make
        // 1 -> Cooks' Assistant
        // 2 -> Sheep Shearer
        // 3 -> Romeo & Juliet

        int notStartedColor = 16711680;
        int inProgressColor = 16776960;
        int finishedColor = 901389;

        // checks quests's assistant
        int textColor = Interfaces.getComponent(399, 6,1).getTextColor();
        if (textColor == notStartedColor) {
            Log.info("Cook's assistant has not been started");
            currentQuest = 1;
            c_state = Cook_state.GO_COOK;
        } else if (textColor == inProgressColor) {
            Log.info("Cook's assistant is in progress");
            currentQuest = 1;
            CooksAssistantcheck();
        } else if (textColor == finishedColor) {
            Log.info("Cook's assistant is finished");
            // checks lesheep shearer
            textColor = Interfaces.getComponent(399, 6,16).getTextColor();
            if (textColor == notStartedColor) {
                Log.info("Sheep Shearer has not been started");
                currentQuest = 2;
                s_state = Sheep_state.CHECK;
            } else if (textColor == inProgressColor) {
                Log.info("Sheep Shearer is in progress");
                currentQuest = 2;
                SheepShearercheck();
            } else if (textColor == finishedColor) {
                Log.info("Sheep Shearer is finished");
                // checks Romeo & Juliet
                textColor = Interfaces.getComponent(399, 6,14).getTextColor();
                if (textColor == notStartedColor) {
                    Log.info("Romeo & Juliet has not been started");
                    currentQuest = 3;
                    //romeo_state = 1;
                } else if (textColor == inProgressColor) {
                    Log.info("Romeo & Juliet is in progress");
                    currentQuest = 3;
                    //RomeoAndJulietcheck();
                } else if (textColor == finishedColor) {
                    Log.info("Romeo & Juliet is finished");
                }
            }
        }
    }

    @Override
    public void onStart() {
//        Log.fine(Game.getClient().isLoginWorldSelectorOpen());
//        String arguments = getArgs();
//        if ((arguments != null && arguments.length() > 0 && arguments.contains("-randomWorld true")) || Game.getClient().isMembersWorld()){
//            if(!Game.isLoggedIn()){
//                sleepUntilForDuration(() -> Game.isOnCredentialsScreen(), 3000, 30000);
//                Game.getClient().setLoginWorldSelectorOpen(true);
//                try {
//                    sleepUntilForDuration(() -> Game.getClient().isLoginWorldSelectorOpen(), 3000, 30000);
//                    Log.fine(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]);
//                    Game.getClient().setWorld(Worlds.get(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]));
//                    Game.getClient().setLoginWorldSelectorOpen(false);
//                    sleepUntil(() -> !Game.getClient().isLoginWorldSelectorOpen(), 1000, 15000);
//                }catch (Exception E){
//                    try {
//                        sleepUntilForDuration(() -> Game.getClient().isLoginWorldSelectorOpen(), 3000, 30000);
//                        Log.fine(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]);
//                        Game.getClient().setWorld(Worlds.get(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]));
//                        Game.getClient().setLoginWorldSelectorOpen(false);
//                        sleepUntil(() -> !Game.getClient().isLoginWorldSelectorOpen(), 1000, 15000);
//                    }catch (Exception A){
//                        sleepUntilForDuration(() -> Game.getClient().isLoginWorldSelectorOpen(), 3000, 30000);
//                        Log.fine(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]);
//                        Game.getClient().setWorld(Worlds.get(mundosF2P[Random.nextInt(0, mundosF2P.length - 1)]));
//                        Game.getClient().setLoginWorldSelectorOpen(false);
//                        sleepUntil(() -> !Game.getClient().isLoginWorldSelectorOpen(), 1000, 15000);
//                    }
//                }
//
//            }
//        }
        //Log.fine();//green
        Log.info("Bot has been initialized!");//grey
        //Log.severe();//red
    }

    @Override
    public int loop() {
        if(run_once)
            switch (currentQuest) {
                case 1:
                    // cooks assistant
                    executeCooksAssistant();
                    break;
                case 2:
                    // lesheep shearer
                    executeSheepShearer();
                    break;
                case 3:
                    // romeo and juliet
                    //executeRomeoAndJuliet();
                    break;
                case 0:
                    Log.fine("All Quests have been finished!");
                    Game.logout();
                    setStopping(true);
                    break;
            }
        else if(Game.getState() != Game.STATE_IN_GAME)
            sleepUntil(() -> Game.getState() == Game.STATE_IN_GAME, 2000, Random.nextInt(40000, 50000));
        else if(Interfaces.getComponent(399, 6,1) != null)
            evaluateQuestsState();
        return Random.nextInt(1000,2000);
    }

    @Override
    public void onStop() {
        Log.severe("Y U DO DIS!");
    }

    private void CooksAssistantcheck() {
        if (!Inventory.contains("Egg"))
            c_state = Cook_state.GO_EGG;
        if (!Inventory.contains("Bucket of milk"))
            c_state = Cook_state.GO_COW;
        if (!Inventory.contains("Grain"))
            c_state = Cook_state.GO_WHEAT;
        if (!Inventory.contains("Pot of flour"))
            c_state = Cook_state.GO_MILL;
        else
            c_state = Cook_state.GO_COOK;
    }

    private void executeCooksAssistant(){
        Log.info("cook_state = " + c_state);
        switch (c_state){
            case GO_COOK:
                if(COOK_AREA.contains(Players.getLocal())){
                    c_state = Cook_state.TALK_COOK;
                    break;
                }
                else
                    walk(COOK_AREA);
                break;
            case TALK_COOK:
                if(Interfaces.getComponent(277,17) != null && Interfaces.getComponent(277,0).isVisible()){
                    sleepUntil(() -> Interfaces.getComponent(277,17).click(),25,Random.nextInt(4000, 6000));
                    evaluateQuestsState();
                    break;
                }
                else{
                    sleepUntil(() ->!Players.getLocal().isMoving(),25,Random.nextInt(4000, 6000));
                    if(Dialog.isOpen() && Dialog.canContinue()) {
                        Dialog.process("Click here");
                    }
                    else if(Dialog.isViewingChatOptions() && !Inventory.contains("Pot of flour")){
                        if(Dialog.process("What's wrong?"))
                            sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                        else if(Dialog.process("I'm always happy to help a cook in distress."))
                            sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                        else if(Dialog.process("Actually, I know where to find this stuff.")) {
                            sleepUntil(() -> Dialog.processContinue(),25, Random.nextInt(4000, 6000));
                            c_state = Cook_state.GO_EGG;
                            break;
                        }
                    }
                    else{
                        Npc npc_cook =  Npcs.getNearest(f -> (f.getName().equals("Cook")));
                        npc_cook.interact("Talk-to");
                        sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                    }
                }
                break;
            case GO_EGG:
                if(EGG_AREA.contains(Players.getLocal())){
                    c_state = Cook_state.GET_EGG;
                    break;
                }
                walk(EGG_AREA);
                break;
            case GET_EGG:
                Pickable egg = Pickables.getNearest("Egg");
                if(egg != null)
                    if(egg.interact("Take"))
                        if (sleepUntil(() -> Inventory.contains("Egg"),25, 8000L)) {
                            c_state = Cook_state.GO_COW;
                            break;
                        }
                break;
            case GO_COW:
                if(COWS_AREA.contains(Players.getLocal())){
                    c_state = Cook_state.GET_MILK;
                    break;
                }
                walk(COWS_AREA);
                break;
            case GET_MILK:
                if(Inventory.contains("Bucket of milk"))
                    c_state = Cook_state.GO_WHEAT;
                else if(!Inventory.contains("Bucket")){
                    Log.severe("No bucket!");
                    setStopping(true);
                    break;
                }
                else{
                    SceneObject cow = SceneObjects.getNearest("Dairy cow");
                    if(cow != null)
                        if(cow.interact("Milk")) {
                            if (sleepUntil(() -> Inventory.contains("Bucket of milk"),25, Random.nextInt(8000, 10000)))
                                c_state = Cook_state.GO_WHEAT;
                        }
                }
            case GO_WHEAT:
                if(WHEAT_AREA.contains(Players.getLocal())){
                    c_state = Cook_state.GET_WHEAT;
                    break;
                }
                walk(WHEAT_AREA);
                break;
            case GET_WHEAT:
                if(Inventory.contains("Grain")) {
                    c_state = Cook_state.GO_MILL;
                    break;
                }
                else{
                    SceneObject wheat = SceneObjects.getNearest("Wheat");
                    if(wheat != null)
                        if(wheat.interact("Pick")) {
                            if (sleepUntil(() -> Inventory.contains("Grain"),25, Random.nextInt(8000, 10000)))
                                c_state = Cook_state.GO_MILL;
                            break;
                        }
                }
                break;
            case GO_MILL:
                if(MILL_AREA.contains(Players.getLocal())){
                    c_state = Cook_state.UP_MILL;
                    break;
                }
                walk(MILL_AREA);
                break;
            case UP_MILL:
                SceneObject stairs = SceneObjects.getNearest("Ladder");
                if(Players.getLocal().getFloorLevel() != 2 && stairs != null) {
                    if (stairs.interact("Climb-up")) {
                        sleepUntil(() -> !Players.getLocal().isAnimating(), 25, Random.nextInt(7000, 9000));
                        break;
                    }
                }
                else
                    c_state = Cook_state.USE_HOPPER;
                break;
            case USE_HOPPER:
                if(Inventory.contains("Grain")){
                    SceneObject hopper = SceneObjects.getNearest("Hopper");
                    if(hopper.interact("Fill")){
                        sleepUntil(() ->!Inventory.contains("Grain"),25,Random.nextInt(7000, 9000));
                        break;
                    }
                }
                break;
            case OPERATE_MILL:
                SceneObject controls = SceneObjects.getNearest("Hopper controls");
                if(controls.interact("Operate")){
                    sleepUntil(() ->Players.getLocal().isAnimating(),25,Random.nextInt(7000, 9000));
                    sleepUntil(() ->!Players.getLocal().isAnimating(),25,Random.nextInt(7000, 9000));
                }
                break;
            case DOWN_MILL:
                SceneObject stairs2 = SceneObjects.getNearest("Ladder");
                if(Players.getLocal().getFloorLevel() != 0 && stairs2 != null) {
                    if (stairs2.interact("Climb-down")) {
                        sleepUntil(() -> !Players.getLocal().isAnimating(), 25, Random.nextInt(7000, 9000));
                        break;
                    }
                }
                else
                    c_state = Cook_state.GET_FLOUR;
                break;
            case GET_FLOUR:
                SceneObject flour = SceneObjects.getNearest("Flour bin");
                if(flour.interact("Empty")){
                    sleepUntil(() ->Inventory.contains("Pot of flour"),25,Random.nextInt(7000, 9000));
                }
                break;
        }
    }

    private void SheepShearercheck(){
        if(Varps.get(179) == 1){
            s_state = Sheep_state.USE_WHEEL;
        }

    }

    private void executeSheepShearer() {
        Log.info("sheep_state = " + s_state);
        Log.fine("Progress-> " + Varps.get(315));
        Log.fine("Status -> " + Varps.get(179));
        switch (s_state){
            case CHECK:
                if(Inventory.getCount() >= 8){
                    if(BANK_AREA.contains(Players.getLocal())){
                        Bank.open(BankLocation.getNearestBooth());
                        if(sleepUntil(() -> Bank.isOpen(),25, Random.nextInt(5000, 7000))){
                            if(sleepUntil(() -> Bank.depositAllExcept("Shears","Wool"),25, Random.nextInt(15000, 20000)))
                                if(Inventory.getCount() <= 8) {
                                    Bank.close();
                                    if (sleepUntil(() -> Bank.isClosed(), 25, Random.nextInt(15000, 20000))) {
                                        s_state = Sheep_state.GO_FARMER;//TODO: change to sheepshearcheck to be able to send it back if anything happens getting the wool
                                        break;
                                    }
                                }
                        }
                    }
                    else
                        walk(BANK_AREA);
                }
                else
                    s_state = Sheep_state.GO_FARMER;
                break;
            case GO_FARMER:
                if(SHEAR_AREA.contains(Players.getLocal())){
                    s_state = Sheep_state.GET_SHEAR;
                    break;
                }
                else
                    walk(SHEAR_AREA);
                break;
            case GET_SHEAR:
                if(!Inventory.contains("Shear")) {
                    Pickable shear = Pickables.getNearest("Shears");
                    if (shear != null) {
                        sleepUntil(() -> shear.interact("Take"), 25, Random.nextInt(5000, 7000));
                        if (sleepUntil(() -> Inventory.contains("Shears"), 25, Random.nextInt(5000, 7000))) {
                            s_state = Sheep_state.TALK_FARMER;
                            break;
                        } else
                            Time.sleep(100000);
                    }
                }
                else
                    s_state = Sheep_state.TALK_FARMER;
                break;
            case TALK_FARMER:
                if(Interfaces.getComponent(277,17) != null && Interfaces.getComponent(277,15).isVisible()){
                    sleepUntil(() -> Interfaces.getComponent(277,15).click(),25,Random.nextInt(4000, 6000));
                    evaluateQuestsState();
                    break;
                }
                if(Varps.get(179) == 1){
                    if(Random.nextBoolean()){
                        s_state = Sheep_state.GO_SHEEP;
                        break;
                    }
                }
                if(Dialog.isOpen() && Dialog.canContinue()) {
                    Dialog.process("Click here");
                }
                else if(Dialog.isViewingChatOptions() && !Inventory.contains("Ball of wool")){
                    if(Dialog.process("I'm looking for a quest."))
                        sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                    else if(Dialog.process("Yes okay. I can do that."))
                        sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                    else if(Dialog.process("Of course!")) {
                        sleepUntil(() -> Dialog.processContinue(), 25, Random.nextInt(4000, 6000));
                        if(Random.nextBoolean()){
                            s_state = Sheep_state.GO_SHEEP;
                            break;
                        }
                    }
                    else if(Dialog.process("I'm something of an expert actually!")) {
                        sleepUntil(() -> Dialog.processContinue(), 25, Random.nextInt(4000, 6000));
                        if(Random.nextBoolean()){
                            s_state = Sheep_state.GO_SHEEP;
                            break;
                        }
                    }
                }
                else if(Dialog.isViewingChatOptions() && Inventory.contains("Ball of wool")){
                    if(Dialog.process("I'm back.")){
                        sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                        break;
                    }
                }
                else{
                    Npc npc_shear =  Npcs.getNearest(f -> (f.getName().equals("Fred the Farmer")));
                    npc_shear.interact("Talk-to");
                    sleepUntil(() -> Dialog.processContinue(),25,Random.nextInt(4000, 6000));
                }
                break;
            case GO_SHEEP:
                if(SHEEP_AREA.contains(Players.getLocal())){
                    s_state = Sheep_state.GET_WOOL;
                    break;
                }
                else
                    walk(SHEEP_AREA);
                break;
            case GET_WOOL:
                Npc sheep = Npcs.getNearest(s -> s.getName().equals("Sheep") && s.getId() != 731);
                if(sheep == null){
                    Movement.walkTo(SHEEP_AREA.getCenter().randomize(5));
                    //sleepUntil(() -> !Players.getLocal().isMoving(),25, Random.nextInt(5000, 7000));
                }
                else {
                    if (Inventory.getCount("Wool") < 20) {
                        if (!Inventory.isFull()) {
                            sheep.interact("Shear");//TODO:still not working properly
                            sleepUntil(() -> Players.getLocal().isAnimating(),25, Random.nextInt(5000, 7000));
                            sleepUntil(() -> !Players.getLocal().isAnimating(),25, Random.nextInt(5000, 7000));
                        } else {
                            s_state = Sheep_state.CHECK;
                            break;
                        }
                    } else
                        s_state = Sheep_state.GO_WHEEL;
                }
                break;
            case GO_WHEEL:
                if(WHEEL_AREA.contains(Players.getLocal())){
                    s_state = Sheep_state.USE_WHEEL;
                    break;
                }
                else
                    walk(WHEEL_AREA);
                break;
            case USE_WHEEL:
                if(Inventory.getCount("Ball of wool") < 20) {
                    if(Inventory.getCount("Wool") != 0) {
                        SceneObject wheel = SceneObjects.getNearest("Spinning wheel");
                        wheel.interact("Spin");//TODO:not working
                        sleepUntil(() -> Interfaces.isOpen(270), 25, Random.nextInt(5000, 7000));
                        Interfaces.getComponent(270, 14).interact("Spin");
                        sleepUntilForDuration(() -> !Players.getLocal().isAnimating(), Random.nextInt(1500, 1700), 25, Random.nextInt(5000, 7000));
                    }
                    else
                        s_state = Sheep_state.GO_SHEEP;
                }
                else
                    s_state = Sheep_state.GO_FARMER;
                break;
        }
    }

    private void walk(Area TargetArea) {
        Player local = Players.getLocal();
        if (sleepUntil(() -> !local.isMoving() || Movement.getDestinationDistance() < Random.nextInt(5, 8),25, Random.nextInt(5000, 7000))){
            if (!TargetArea.contains(local)){
                if(Movement.getRunEnergy() > Random.nextInt(8, 15) && !Movement.isRunEnabled())
                    Movement.toggleRun(true);
                Movement.walkTo(TargetArea.getCenter().randomize(Random.nextInt(1, 4)));
            }
        }
    }

    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        if (chatMessageEvent.getMessage().contains("You put the grain in the hopper. ")) {
            c_state = Cook_state.OPERATE_MILL;
        }
        if (chatMessageEvent.getMessage().contains("You operate the hopper. ")) {
            c_state = Cook_state.DOWN_MILL;
        }
        if (chatMessageEvent.getMessage().contains("You operate the empty hopper. ")) {
            c_state = Cook_state.DOWN_MILL;
        }
        if (chatMessageEvent.getMessage().contains("You fill a pot with ")) {
            c_state = Cook_state.GO_COOK;
        }
        if (chatMessageEvent.getMessage().contains("The flour bin is already empty. ")) {
            c_state = Cook_state.GO_COOK;
        }
    }

}