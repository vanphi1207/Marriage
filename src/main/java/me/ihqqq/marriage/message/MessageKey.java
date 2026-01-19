package me.ihqqq.marriage.message;


public enum MessageKey {
    
    PREFIX("prefix"),
    
    HELP_HEADER("help-header"),
    HELP_MENU("help-menu"),
    HELP_PROPOSE("help-propose"),
    HELP_ACCEPT("help-accept"),
    HELP_DENY("help-deny"),
    HELP_DIVORCE("help-divorce"),
    HELP_INFO("help-info"),
    HELP_TP("help-tp"),
    HELP_SETHOME("help-sethome"),
    HELP_HOME("help-home"),
    HELP_CHAT("help-chat"),
    HELP_MSG("help-msg"),
    HELP_GIFT("help-gift"),
    HELP_RING("help-ring"),
    HELP_RELOAD("help-reload"),
    
    PROPOSE_SENT("propose-sent"),
    PROPOSE_RECEIVED("propose-received"),
    PROPOSE_ALREADY_SENT("propose-already-sent"),
    PROPOSE_COOLDOWN("propose-cooldown"),
    PROPOSE_SELF("propose-self"),
    ALREADY_MARRIED("already-married"),
    TARGET_ALREADY_MARRIED("target-already-married"),
    NO_PROPOSAL("no-proposal"),
    ACCEPT_SUCCESS("accept-success"),
    DENY_SUCCESS("deny-success"),
    PROPOSE_DENIED("propose-denied"),
    DIVORCE_CONFIRM("divorce-confirm"),
    DIVORCE_SUCCESS("divorce-success"),
    NOT_MARRIED("not-married"),
    INFO_SINGLE("info-single"),
    INFO_MARRIED("info-married"),
    INFO_SINGLE_OTHER("info-single-other"),
    INFO_MARRIED_OTHER("info-married-other"),
    TP_DISABLED("tp-disabled"),
    TP_SUCCESS("tp-success"),
    SETHOME_SUCCESS("sethome-success"),
    HOME_SUCCESS("home-success"),
    HOME_NOT_SET("home-not-set"),
    CHAT_TOGGLED("chat-toggled"),
    MSG_FORMAT("msg-format"),
    MSG_FORMAT_RECEIVED("msg-format-received"),
    RELOAD_SUCCESS("reload-success"),
    
    PLAYER_NOT_FOUND("player-not-found"),
    NO_PERMISSION("no-permission"),
    NOT_A_PLAYER("not-a-player"),
    PARTNER_OFFLINE("partner-offline"),
    CHAT_DISABLED("chat-disabled"),
    RING_UNIMPLEMENTED("ring-unimplemented"),
    
    ADMIN_SET_SUCCESS("admin-set-success"),
    ADMIN_DIVORCE_SUCCESS("admin-divorce-success"),
    ADMIN_INFO_MARRIED("admin-info-married"),
    ADMIN_INFO_SINGLE("admin-info-single"),
    
    ADMIN_HELP("admin-help"),
    ADMIN_HELP_SET("admin-help-set"),
    ADMIN_HELP_DIVORCE("admin-help-divorce"),
    ADMIN_HELP_INFO("admin-help-info"),
    ADMIN_HELP_RELOAD("admin-help-reload");

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    
    public String getPath() {
        return path;
    }
}