package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

data class GuideSection(
    val id: String,
    val icon: ImageVector,
    val titleEn: String,
    val titleHi: String,
    val summaryEn: String,
    val summaryHi: String,
    val detailsEn: List<String>,
    val detailsHi: List<String>,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGuideDialog(
    onDismiss: () -> Unit,
    onNavigateToShield: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAppSelector: () -> Unit
) {
    var isHindi by remember { mutableStateOf(false) }
    var expandedSectionId by remember { mutableStateOf<String?>("section_modes") }

    val sections = remember {
        listOf(
            GuideSection(
                id = "section_overview",
                icon = Icons.Default.Info,
                titleEn = "1. Overview & Mission",
                titleHi = "1. ऐप का परिचय और उद्देश्य",
                summaryEn = "FOCUS OS turns your smartphone into a dedicated study terminal with zero digital distractions.",
                summaryHi = "FOCUS OS आपके स्मार्टफोन को एक समर्पित स्टडी टर्मिनल में बदल देता है ताकि पढ़ाई में 100% एकाग्रता रहे।",
                detailsEn = listOf(
                    "Built specifically for competitive exam aspirants (UPSC, JEE, NEET, SSC) and serious students.",
                    "Combines strict app blocking, scheduled study timetables, camera verification, and deep study analytics.",
                    "Completely prevents habitual doomscrolling on Instagram, YouTube Shorts, Reels, and mobile games."
                ),
                detailsHi = listOf(
                    "यह विशेष रूप से प्रतियोगी परीक्षाओं (UPSC, JEE, NEET, SSC) और गंभीर छात्रों के लिए बनाया गया है।",
                    "यह स्ट्रिक्ट ऐप ब्लॉकिंग, शेड्यूल टाइमटेबल, कैमरा वेरिफिकेशन और डीप एनालिटिक्स को जोड़ता है।",
                    "इंस्टाग्राम रील्स, यूट्यूब शॉर्ट्स और गेम्स की लत से पूरी तरह छुटकारा दिलाता है।"
                ),
                tag = "BASICS"
            ),
            GuideSection(
                id = "section_quick_focus",
                icon = Icons.Default.Bolt,
                titleEn = "2. Quick Focus Mode",
                titleHi = "2. क्विक फोकस मोड",
                summaryEn = "Instant Pomodoro or custom duration timers with optional binaural background audio.",
                summaryHi = "तुरंत पोमोडोरो या कस्टम टाइमर शुरू करें, साथ में बैकग्राउंड साउंड भी उपलब्ध है।",
                detailsEn = listOf(
                    "Presets: 25m (Standard Pomodoro), 45m (Lecture), 60m (Deep Work), or custom duration.",
                    "Binaural Beats & Ambient Audio: White Noise, Rain, Forest, Alpha Waves (40Hz) to enhance concentration.",
                    "Subject tagging to track study hours per subject in analytics."
                ),
                detailsHi = listOf(
                    "प्रीसेट्स: 25 मिनट (पोमोडोरो), 45 मिनट (क्लास), 60 मिनट (डीप स्टडी) या अपनी पसंद का समय चुनें।",
                    "बाइनॉरल बीट्स और बैकग्राउंड साउंड: व्हाइट नॉइज़, बारिश, जंगल की आवाज और अल्फा वेव्स।",
                    "सब्जेक्ट टैगिंग जिससे हर विषय के स्टडी आवर्स का पूरा हिसाब रहे।"
                ),
                tag = "TIMER"
            ),
            GuideSection(
                id = "section_schedule",
                icon = Icons.Default.Schedule,
                titleEn = "3. Strict Schedule & Timetable",
                titleHi = "3. स्ट्रिक्ट शेड्यूल व टाइमटेबल",
                summaryEn = "Set fixed daily study routines with automated alarms and auto-start protection.",
                summaryHi = "रोजाना का टाइमटेबल सेट करें। तय समय पर अलार्म बजेगा और सेशन लॉक हो जाएगा।",
                detailsEn = listOf(
                    "Set exact start and end times for your study sessions in advance.",
                    "Pre-Alert: Receives a heads-up notification 2 minutes before the session starts.",
                    "Auto-Boot Restoration: Scheduled alarms are automatically restored even if the phone is rebooted."
                ),
                detailsHi = listOf(
                    "अपनी पढ़ाई के लिए पहले से ही निश्चित समय (जैसे शाम 4 से 6 बजे) सेट करें।",
                    "प्री-अलर्ट: सेशन शुरू होने से 2 मिनट पहले नोटिफिकेशन मिलेगा ताकि आप तैयार हो सकें।",
                    "फोन रीस्टार्ट होने पर भी सारे शेड्यूल्ड अलार्म अपने आप वापस एक्टिव हो जाते हैं।"
                ),
                tag = "SCHEDULE"
            ),
            GuideSection(
                id = "section_modes",
                icon = Icons.Default.Lock,
                titleEn = "4. The 3 Lock Modes",
                titleHi = "4. तीन लॉक सुरक्षा स्तर",
                summaryEn = "Choose the strictness level matching your self-discipline needs.",
                summaryHi = "अपनी जरूरत के हिसाब से लॉक की सख्ती चुनें।",
                detailsEn = listOf(
                    "NORMAL MODE: Basic study timer without app blocking. Ideal for light revision.",
                    "SOFT LOCK (Level 1): Displays a warning full-screen overlay if you attempt to leave FOCUS OS.",
                    "STRICT LOCK (Level 2): Usage Access Monitor + Overlay Shield. Automatically detects and blocks any unwhitelisted app (Instagram, YouTube, Games, WhatsApp).",
                    "MAXIMUM LOCK (Level 3 - Kiosk): Enforces strict kiosk mode with a 200-second emergency penalty timer before exit."
                ),
                detailsHi = listOf(
                    "नॉर्मल मोड: बिना ऐप ब्लॉकिंग के सामान्य टाइमर।",
                    "सॉफ्ट लॉक (लेवल 1): ऐप से बाहर निकलने पर स्क्रीन पर चेतावनी दिखाई देगी।",
                    "स्ट्रिक्ट लॉक (लेवल 2): यूसेज एक्सेस मॉनिटर + शील्ड ओवरले। प्रतिबंधित ऐप (इंस्टाग्राम, यूट्यूब आदि) खुलते ही तुरंत ब्लॉक हो जाएंगे।",
                    "मैक्सिमम लॉक (लेवल 3 - कियोस्क): फुल फोन लॉकडाउन। इमरजेंसी में बाहर निकलने के लिए 200 सेकंड की पेनल्टी लगेगी।"
                ),
                tag = "SECURITY"
            ),
            GuideSection(
                id = "section_whitelist",
                icon = Icons.Default.CheckCircle,
                titleEn = "5. App Whitelist System",
                titleHi = "5. ऐप व्हाइटलिस्ट (अनुमति सूची)",
                summaryEn = "Allow essential study apps (PDF readers, Dictionary, Zoom) while blocking everything else.",
                summaryHi = "केवल पढ़ाई के ऐप्स (PDF, डिक्शनरी, नोट्स) को अनुमति दें, बाकी सब ब्लॉक रहेंगे।",
                detailsEn = listOf(
                    "By default, all distracting apps (Social Media, Video, Games, Browsers) are strictly blocked.",
                    "You can selectively whitelist study companion apps (e.g. Acrobat Reader, Notion, Google Docs).",
                    "Quick Profiles: Switch between 'Strict Zero-Apps' and 'Class Apps' profiles."
                ),
                detailsHi = listOf(
                    "डिफ़ॉल्ट रूप से सभी सोशल मीडिया, गेम्स और वीडियो ऐप्स ब्लॉक रहते हैं।",
                    "आप पढ़ाई से जुड़े जरूरी ऐप्स (जैसे PDF रीडर, नोट्स, ज़ूम) को व्हाइटलिस्ट में जोड़ सकते हैं।",
                    "क्विक प्रोफाइल्स: 'जीरो ऐप' या 'स्टडी क्लास ऐप' प्रोफाइल में आसानी से स्विच करें।"
                ),
                tag = "APPS"
            ),
            GuideSection(
                id = "section_camera",
                icon = Icons.Default.CameraAlt,
                titleEn = "6. Camera Verification System",
                titleHi = "6. कैमरा फोटो वेरिफिकेशन",
                summaryEn = "Desk photo proof before starting and selfie proof at completion prevent fake sessions.",
                summaryHi = "शुरुआत में स्टडी टेबल की फोटो और अंत में सेल्फी लेना अनिवार्य है ताकि कोई चीटिंग न हो सके।",
                detailsEn = listOf(
                    "START PROOF: Must capture a live photo of your study desk / book before the timer will unlock.",
                    "END PROOF: Must take a selfie verification photo when the timer ends to complete the session and unlock your phone.",
                    "Zero Cheating: Both photos are timestamped and saved into your session audit log."
                ),
                detailsHi = listOf(
                    "शुरुआती वेरिफिकेशन: पढ़ाई शुरू करने से पहले किताबों/डेस्क की फोटो लेना जरूरी है।",
                    "फाइनल वेरिफिकेशन: सेशन पूरा होने पर सेल्फी लेने के बाद ही फोन अनलॉक होगा।",
                    "जीरो चीटिंग: दोनों फोटो टाइमस्टैम्प के साथ सेशन रिकॉर्ड में सुरक्षित रहती हैं।"
                ),
                tag = "VERIFY"
            ),
            GuideSection(
                id = "section_shield",
                icon = Icons.Default.Shield,
                titleEn = "7. Permission Shield Architecture",
                titleHi = "7. परमिशन्स शील्ड गाइड",
                summaryEn = "Understanding how Android permissions work together to achieve policy-compliant distraction blocking.",
                summaryHi = "जानिए एंड्रॉइड पर सुरक्षित और सटीक ऐप ब्लॉकिंग के लिए ये परमिशन्स कैसे काम करते हैं।",
                detailsEn = listOf(
                    "1. Usage Access (PACKAGE_USAGE_STATS): Real-time monitor of foreground apps to detect unauthorized app launches.",
                    "2. Draw Over Other Apps (Overlay): Displays strict lock screen shield over blocked apps.",
                    "3. Query Packages: Allows discovering installed apps for custom whitelist selection.",
                    "4. Ignore Battery Optimization: Prevents Android OEM killers from terminating the active focus timer.",
                    "5. Schedule Exact Alarms: Guarantees precise schedule timetable alarms down to the exact second.",
                    "6. Foreground Service (specialUse): Keeps study countdown and audio engines active in background.",
                    "7. Post Notifications: Displays live study timer countdown in the status bar.",
                    "8. Camera: Enables anti-cheat desk photo & selfie verification.",
                    "9. Boot Completed: Re-registers your timetable alarms after phone reboot."
                ),
                detailsHi = listOf(
                    "1. यूसेज एक्सेस: बैकग्राउंड और फोरग्राउंड ऐप्स को मॉनिटर करता है।",
                    "2. डिस्प्ले ओवर अदर ऐप्स: ब्लॉक ऐप्स के ऊपर सुरक्षा लॉक स्क्रीन दिखाता है।",
                    "3. क्वेरी पैकेज: फोन के ऐप्स को व्हाइटलिस्ट करने के लिए स्कैन करता है।",
                    "4. बैटरी ऑप्टिमाइजेशन छूट: बैकग्राउंड सर्विस को कभी बंद नहीं होने देता।",
                    "5. शेड्यूल एग्जैक्ट अलार्म: तय समय पर बिना चूके अलार्म बजाता है।",
                    "6. फोरग्राउंड सर्विस: बैकग्राउंड में स्टडी टाइमर और बाइनॉरल साउंड चालू रखता है।",
                    "7. नोटिफिकेशन्स: स्टेटस बार में टाइमर का लाइव काउंटडाउन दिखाता है।",
                    "8. कैमरा: फोटो और सेल्फी वेरिफिकेशन के लिए।",
                    "9. बूट कम्प्लीट: फोन चालू होने पर टाइमटेबल वापस सेट करता है।"
                ),
                tag = "SHIELD"
            ),
            GuideSection(
                id = "section_streaks",
                icon = Icons.Default.LocalFireDepartment,
                titleEn = "8. Streaks & Analytics",
                titleHi = "8. स्ट्रीक और स्टडी एनालिटिक्स",
                summaryEn = "Real daily focus time, consecutive days streak, and 5-hour daily goal tracking.",
                summaryHi = "असली दैनिक पढ़ाई का समय, लगातार दिनों की स्ट्रीक और 5 घंटे का डेली गोल।",
                detailsEn = listOf(
                    "Streak Calculation: Counts continuous consecutive calendar days where at least one study session was completed.",
                    "Daily Goal: Default 5-hour study target with real-time percentage progress bar.",
                    "Distraction Meter: Logs how many times you attempted to open blocked apps during study."
                ),
                detailsHi = listOf(
                    "स्ट्रीक गणना: हर दिन लगातार पढ़ाई करने पर स्ट्रीक दिन-प्रतिदिन बढ़ती है।",
                    "डेली गोल: रोजाना 5 घंटे की पढ़ाई का लक्ष्य और लाइव प्रोग्रेस बार।",
                    "डिस्ट्रैक्शन मीटर: सेशन के दौरान आपने कितनी बार ब्लॉक ऐप खोलने की कोशिश की उसका रिकॉर्ड।"
                ),
                tag = "STATS"
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "📖 फोकस ओएस संपूर्ण गाइड" else "📖 FOCUS OS USER MANUAL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = if (isHindi) "सभी फीचर्स और सेटिंग्स की पूरी जानकारी" else "Complete Documentation & Feature Guide",
                                style = MaterialTheme.typography.labelSmall,
                                color = FocusTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        // Language Switcher Button
                        FilledTonalButton(
                            onClick = { isHindi = !isHindi },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isHindi) FocusWarning else FocusPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isHindi) "English 🇬🇧" else "हिन्दी 🇮🇳",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FocusSurface,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = FocusBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Quick Navigation Action Pills
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "⚡ क्विक शॉर्टकट्स" else "⚡ QUICK SHORTCUTS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = FocusWarning
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onNavigateToShield()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "शील्ड सेटिंग्स" else "10-Shield", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onNavigateToSchedule()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusWarning),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FocusWarning.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "टाइमटेबल" else "Schedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onNavigateToAppSelector()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FocusSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "व्हाइटलिस्ट" else "Whitelist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Guide Sections
                items(sections, key = { it.id }) { section ->
                    val isExpanded = expandedSectionId == section.id
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isExpanded) FocusPrimary.copy(alpha = 0.6f) else FocusOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSectionId = if (isExpanded) null else section.id
                            }
                            .testTag("guide_section_${section.id}")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Section Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (isExpanded) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = section.icon,
                                            contentDescription = null,
                                            tint = if (isExpanded) FocusPrimary else FocusTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isHindi) section.titleHi else section.titleEn,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = FocusSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = section.tag,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = FocusPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = FocusTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Short Summary
                            Text(
                                text = if (isHindi) section.summaryHi else section.summaryEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusTextSecondary,
                                lineHeight = 18.sp
                            )

                            // Expanded Detailed Points
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    Divider(color = FocusSurfaceVariant, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val points = if (isHindi) section.detailsHi else section.detailsEn
                                    points.forEach { point ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = FocusPrimary,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = point,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.9f),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isHindi) "समझ गया / वापस जाएं" else "GOT IT • CLOSE GUIDE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FocusOnPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
