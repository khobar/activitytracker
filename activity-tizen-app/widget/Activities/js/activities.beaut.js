var active;
var types = {
    DEV: "Developer",
    SM: "Scrum Master"
};

function loadActive() {
    showButtonsBody(false);
    showMinutesBody(false);
    showActiveBody(false);
    get(active_url, setActive);
}

function handleActivity(d) {
    showButtonsBody(false);
    showActiveBody(false);
    var c = {
        type: d
    };
    if (active && d === active.type) {
        log("[DEBUG] Handling stoping of type :" + d);
        post(stop_url, c, afterStoped);
    } else {
        log("[DEBUG] Handling type change :" + d);
        post(start_url, c, setActive);
    }
}

function stopActivity() {
    showButtonsBody(false);
    showActiveBody(false);
    post(stop_url, {}, afterStoped);
}

function afterStoped(b) {
    showMinutes(b);
    setNoActivity();
}

function showMinutes(f) {
    if (f.target.readyState === 4 && f.target.status === 200) {
        if (f.target.responseText) {
            log("[DEBUG] Activity stoped, showing minutes");
            var e = JSON.parse(f.target.responseText);
            var d = e.type;
            document.getElementById("minutes").textContent = "+ " + e.minutes + "m";
            document.getElementById("minutes_type").textContent = types[d];
            showMinutesBody(true);
            setTimeout(function() {
                document.getElementById("minutes").textContent = "";
                document.getElementById("minutes_type").textContent = "";
                showButtonsBody(true);
                showMinutesBody(false);
            }, 3000);
        }
    }
}

function setActive(f) {
    if (f.target.readyState === 4 && f.target.status === 200) {
        log("[DEBUG] Recived data from active");
        if (f.target.responseText) {
            active = JSON.parse(f.target.responseText);
            log("[DEBUG] Active present , setting");
            var d = active.type;
            document.getElementById("active_start").textContent = active.startTime.replace("T", " ");
            document.getElementById("active_time").textContent = active.minutes + "m";
            var e = new Date(active.startTime);
            document.getElementById("active_icon").src = "images/" + d + ".png";
            document.getElementById("active_text").textContent = types[d];
            showActiveBody(true);
        } else {
            log("[DEBUG] No active task , showing buttons, reseting active screen");
            showButtonsBody(true);
            setNoActivity();
        }
    }
}

function setNoActivity() {
    active = {};
    document.getElementById("SM_btn").src = "images/SM.png";
    document.getElementById("DEV_btn").src = "images/DEV.png";
    document.getElementById("active_time").textContent = "";
    document.getElementById("active_text").textContent = "";
    document.getElementById("active_icon").src = "";
    showActiveBody(false);
}