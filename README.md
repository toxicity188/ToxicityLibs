# ToxicityLibs
Paper API 사용자들을 위한 단순하고 간결하면서, 강력한 코어 플러그인 입니다.

해당 플러그인은 다른 플러그인 개발을 지원해주는 간단한 API를 제공합니다.  
마인크래프트 Bukkit 환경에서 가장 범용성 높은 언어인 Java로 개발되었으며, 최신 버젼 Paper을 지원합니다.

## 의존성 추가
``` groovy
repositories {
    mavenCentral()
    maven {
        url = "https://jitpack.io"
    }
}

```
우선, jitpack을 repository에 추가합니다.
``` groovy
dependencies {
    compileOnly 'com.github.toxicity188:ToxicityLibs:master-SNAPSHOT'
}

```
그리고 dependency에 다음과 같이 선언합니다.
## 손쉬운 Component parse 제공
Paper API에서 기본으로 사용되는 Json message 컴포넌트인 ```net.kyori.adventure의 Component```, 다들 써보셨나요?  
강력한 기능을 자랑하지만, String에서 바로 Component로 조합하기 굉장히 번거로운 것이 사실입니다.  
```kor.toxicity.toxicitylibs.api.ComponentReader``` 객체는 이를 보완하여 손쉬운 parsing을 여러분께 제공해 드립니다.

### 생성
``` java
new ComponentReader(String)
```
파싱하고자 하는 문자열을 넘겨 생성하시면 됩니다.
### 메소드

``` java
ComponentReader(String).getResult() //파싱한 결과를 출력합니다.
ComponentReader(String).buildPlaceholders(Player) //PlaceholderAPI를 이용해 필드로 주어진 문자열을 추가로 파싱합니다.
```
### 관련 커멘드
/tc pa - ```ComponentReader(String).getResult()```의 실제 동작을 인게임에서 확인하실 수 있습니다.  
/tc ph - ```ComponentReader(String).buildPlaceholders(Player)```의 실제 동작을 인게임에서 확인하실 수 있습니다.
![all](https://github.com/toxicity188/ToxicityLibs/assets/114675706/fb2564e8-91be-411b-beaf-073e9af43703)  
![place](https://github.com/toxicity188/ToxicityLibs/assets/114675706/5831e9c6-436a-4ab9-8120-d289e2700612)  
![feef처](https://github.com/toxicity188/ToxicityLibs/assets/114675706/ef16a7f0-1134-49b1-a446-e47ded4c3ecb)  
![eee](https://github.com/toxicity188/ToxicityLibs/assets/114675706/2ac75066-ef35-434c-b880-bddff35ad02b)  
![font](https://github.com/toxicity188/ToxicityLibs/assets/114675706/f04cff18-30b2-44dc-a583-07f062832dbe)  
![222처](https://github.com/toxicity188/ToxicityLibs/assets/114675706/60ccdc8b-2c35-4886-a182-564a63d62f81)
## 간결하나 강력한 커멘드 라이브러리 제공
MythicMobs 같이 일반적으로 거대한 프로젝트의 커멘드 구성은 굉장히 복잡합니다. 해당 커멘드 라이브러리는 그러한 프로젝트에서 강점을 발휘합니다.  
```kor.toxicity.toxicitylibs.api.command.CommandAPI```는 큰 노력을 들이지 않고 그럴싸한 퀄리티의 커멘드 체계를 구현할 수 있게 도와줍니다.

### 생성
``` java
new CommandAPI(String)
```
Prefix가 될 문자열을 넘겨 생성하시면 됩니다.
### 예제
``` java
    private final CommandAPI commandAPI = new CommandAPI("<gradient:blue-aqua>[ToxicityLibs]")
            .setCommandPrefix("tc") //help 명령어를 통해 출력될 prefix
            //parse
            .create("parse") //커멘드 빌더 생성
            .setAliases(new String[] {"pa"}) //해당 커멘드로 입력해도 해당 빌더의 executor이 실행
            .setDescription("parse your argument.") //커멘드의 help 상 설명
            .setUsage("parse <text>") //커멘드의 help 상 사용법
            .setPermission(new String[] {"toxicitylibs.parse"}) //펄미션
            .setLength(1) //executor가 요구하는 args 길이
            .setExecutor((c,a) -> c.sendMessage(StringUtil.colored(String.join(" ",a)))) //커멘드가 실행되었을 때 동작
            .build()
            //reload
            .create("reload")
            .setAliases(new String[] {"re","rl"})
            .setDescription("reload this plugin.")
            .setUsage("reload")
            .setPermission(new String[] {"toxicitylibs.reload"})
            .setExecutor((c,a) -> reload(l -> getCommandAPI().message(c,"plugin reloaded (" + l + " ms)")))
            .build()
            //placeholder
            .create("placeholder")
            .setAliases(new String[] {"ph"})
            .setDescription("parse your argument with PlaceholderAPI.")
            .setUsage("placeholder <text>")
            .setPermission(new String[] {"toxicitylibs.placeholder"})
            .setLength(1)
            .setAllowedSender(new SenderType[] {SenderType.PLAYER})
            .setExecutor((c,a) -> c.sendMessage(new ComponentReader(String.join(" ",a)).buildPlaceholders((Player) c)))
            .build()
            //give
            .create("give")
            .setAliases(new String[] {"g"})
            .setDescription("give handheld item to some player.")
            .setUsage("give <player> [second]")
            .setLength(1)
            .setPermission(new String[] {"toxicitylibs.give"})
            .setAllowedSender(new SenderType[] {SenderType.PLAYER})
            .setExecutor((c,a) -> {
                var item = ((Player) c).getInventory().getItemInMainHand();
                Long left = null;
                var target = Bukkit.getOfflinePlayer(a[0]);
                if (a.length > 1) {
                    try {
                        left = Long.parseLong(a[1]);
                    } catch (Exception e) {
                        getCommandAPI().message(c,"this is not an integer: " + a[1]);
                    }
                }
                long actualLeft = left != null ? left : -1;
                var thread = PLAYER_THREAD_MAP.get(target.getUniqueId());
                var now = LocalDateTime.now();
                if (thread != null) {
                    var data = thread.data;
                    data.getStorageItem().add(new ItemData(now,item,actualLeft));
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(ToxicityLibs.this,() -> {
                        var db = ToxicityConfig.INSTANCE.getCurrentDatabase();
                        var data = db.load(ToxicityLibs.this, target);
                        data.getStorageItem().add(new ItemData(now,item,actualLeft));
                        db.save(ToxicityLibs.this, target, data);
                    });
                }
                getCommandAPI().message(c,"your item successfully be given.");
            })
            .build()
            ;
```
``` java
        crates.getCommandAPI()
                .create("give")
                .setAliases(new String[] {"g"})
                .setDescription("give the item.")
                .setUsage("give <name> <crate name> [amount]")
                .setLength(2)
                .setPermission(new String[] {"customcrates.give"})
                .setExecutor((c,a) -> {
                    var player = Bukkit.getPlayer(a[0]);
                    if (player == null) {
                        crates.getCommandAPI().message(c,"unable to find that player: " + a[0]); //해당 커멘드API상 prefix로 플레이어에게 메시지 전송
                        return;
                    }
                    var crate = CRATE_MAP.get(a[1]);
                    if (crate == null) {
                        crates.getCommandAPI().message(c, "unable to find that crate: " + a[1]);
                        return;
                    }
                    var amount = 1;
                    if (a.length > 2) {
                        try {
                            amount = Integer.parseInt(a[2]);
                        } catch (Exception e) {
                            crates.getCommandAPI().message(c,"this is not an integer: " + a[2]);
                        }
                    }

                    var data = CustomCrates.getPlayerData(player);
                    if (data != null) {
                        data.add(crate,amount);
                    }
                    crates.getCommandAPI().message(c, "the crate successfully be given to player " + player.getName());
                })
                .setTabCompleter((c,a) -> a.length == 2 ? CRATE_MAP.keySet().stream().filter(s -> s.startsWith(a[1])).toList() : null) //tabCompleter 선언
                .build()
        ;
```
### 결과
![녹화_2023_08_31_05_58_49_948](https://github.com/toxicity188/ToxicityLibs/assets/114675706/15a4cde2-428b-44b2-86dd-a5b1fa92cc7d)  
![캡처_2023_08_31_05_57_30_472](https://github.com/toxicity188/ToxicityLibs/assets/114675706/d631d78d-98f9-41c2-8e61-ddc9630400aa)  
![캡처_2023_08_31_05_58_12_555](https://github.com/toxicity188/ToxicityLibs/assets/114675706/b13a1076-44b1-4bdb-b201-d8edb1c7491c)
## 간단한 GUI 보조 툴 제공
별도의 GUI 유틸 없이 플러그인을 만든다는 것은 상상하기 어려운 일입니다. 왜냐하면 요즘 플러그인의 GUI 역시 굉장히 복합적이기 때문입니다.  
ToxicityAPI에선 다음과 같은 GUI 솔루션을 제공합니다.

### 코드
``` java
ToxicityAPI.getInstance().getGuiManager().openGui(Player, GuiExecutor)
```
ToxicityAPI에서 Gui 매니저를 가져옵니다.  
그리고 GuiExecutor을 구현한 객체를 넘겨주시면 됩니다.

### 예제
``` java
        ToxicityAPI.getInstance().getGuiManager().openGui(player, new GuiExecutor(54, CustomCrates.CONFIG.getGuiName().buildPlaceholders(player)) {
            @Override
            public void initialize() {
                //해당 GUI가 오픈될 때 코드
            }

            @Override
            public boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button) {
                //해당 GUI에서 아이템을 클릭할 때 코드
                return true; //아이템 클릭 이벤트를 캔슬할 지 여부
            }
            @Override
            public void onEnd() {
                //해당 GUI가 닫힐 때 코드
            }
        },GuiType.DEFAULT);
```
