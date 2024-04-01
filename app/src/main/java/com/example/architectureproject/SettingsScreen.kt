package com.example.architectureproject

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import com.example.architectureproject.ui.theme.*

class SettingsScreen : Screen {
    private fun mailto(address: String, subject: String) {
        val mailtoUri = Uri.fromParts("mailto", address, null)
        val intent = Intent(Intent.ACTION_SENDTO, mailtoUri)
        intent.putExtra(Intent.EXTRA_EMAIL, address)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        GreenTraceProviders.getActivity()
            .startActivity(Intent.createChooser(intent, "Send Email"))
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(48.dp)) {
                    IconButton(onClick = { navigator?.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("App Settings", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.height(100.dp))

            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = rememberVectorPainter(Icons.Filled.Star),
                    contentDescription = "Dark Mode",
                    modifier = Modifier.size(32.dp),
                    tint = Green40,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Dark Mode", color = darkGreen2)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { mailto("test@example.com", "GreenTrace Feedback") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.MailOutline),
                    contentDescription = "Contact Us",
                    modifier = Modifier.size(32.dp),
                    tint = Green40,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Contact Us", color = darkGreen2)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navigator?.push(PrivacyPolicyScreen()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Filled.Info),
                    contentDescription = "Privacy Policy",
                    modifier = Modifier.size(32.dp),
                    tint = Green40,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Privacy Policy", color = darkGreen2)
            }
        }
    }
}

class PrivacyPolicyScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigator?.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                    }
                }

                Text("PRIVACY POLICY", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ContentTxt(a = "Last updated March 04, 2024")
                TitleTxt(a = "1. WHAT INFORMATION DO WE COLLECT?")
                SecondaryTitleTxt("Personal information you disclose to us\n")
                ContentTxt(
                    a = "In Short: We collect personal information that you provide to us.\n" +
                            "\n" +
                            "We collect personal information that you voluntarily provide to us when you register on the Services, express an interest in obtaining information about us or our products and Services, when you participate in activities on the Services, or otherwise when you contact us.\n" +
                            "\n" +
                            "Personal Information Provided by You. The personal information that we collect depends on the context of your interactions with us and the Services, the choices you make, and the products and features you use. The personal information we collect may include the following:\n" +
                            "\n" +
                            "- names\n" +
                            "- email addresses\n" +
                            "- usernames\n" +
                            "- passwords\n" +
                            "\n"
                )
                CustomStyledText(
                    "Sensitive Information.",
                    "We do not process sensitive information.\n"
                )
                CustomStyledText(
                    "Application Data.",
                    " If you use our application(s), we also may collect the following information if you choose to provide us with access or permission:\n"
                )
                CustomStyledTextItalic(
                    "- Geolocation information.",
                    " We may request access or permission to track location-based information from your mobile device, either continuously or while you are using our mobile application(s), to provide certain location-based services. If you wish to change our access or permissions, you may do so in\n" +
                            "your device's settings.\n"
                )
                CustomStyledTextItalic(
                    "- Push Notifications.",
                    " We may request to send you push notifications regarding your account or certain features of the application(s). If you wish to opt out from receiving these types of communications, you may turn them off in your device's settings.\n"
                )
                ContentTxt(
                    a = "\n" +
                            "This information is primarily needed to maintain the security and operation of our application, for troubleshooting, and for our internal analytics and reporting purposes.\n" +
                            "\n" +
                            "All personal information that you provide to us must be true, complete, and accurate, and you must notify us of any changes to such personal information.\n" +
                            "\n"
                )
                SecondaryTitleTxt(a = "Information automatically collected\n")
                ContentTxt(
                    a =
                    "In Short: Some information — such as your Internet Protocol (IP) address and/or\n" +
                            "browser and device characteristics — is collected automatically when you visit our\n" +
                            "Services." +
                            "\n" +
                            "We automatically collect certain information when you visit, use, or navigate the\n" +
                            "Services. This information does not reveal your specific identity (like your name or\n" +
                            "contact information) but may include device and usage information, such as your IP\n" +
                            "address, browser and device characteristics, operating system, language\n" +
                            "preferences, referring URLs, device name, country, location, information about how\n" +
                            "and when you use our Services, and other technical information. This information is\n" +
                            "primarily needed to maintain the security and operation of our Services, and for our\n" +
                            "internal analytics and reporting purposes.\n" +
                            "\n" +
                            "Like many businesses, we also collect information through cookies and similar\n" +
                            "technologies.\n" +
                            "\n" +
                            "The information we collect includes:\n"
                )
                CustomStyledTextItalic(
                    title = "- Log and Usage Data.",
                    content = "Log and usage data is service-related, diagnostic, usage, and performance information our servers automatically collect when you access or use our Services and which we record in log files. Depending on how you interact with us, this log data may include your IP address, device information, browser type, and settings and information about your activity in the Services (such as the date/time stamps associated with your usage, pages and files viewed, searches, and other actions you take such as which features you use), device event information (such as system activity, error reports (sometimes called \"crash dumps\"), and hardware settings)."
                )
                CustomStyledTextItalic(
                    title = "- Device Data.",
                    content = "We collect device data such as information about your computer," +
                            "phone, tablet, or other device you use to access the Services. Depending on" +
                            "the device used, this device data may include information such as your IP" +
                            "address (or proxy server), device and application identification numbers," +
                            "location, browser type, hardware model, Internet service provider and/or" +
                            "mobile carrier, operating system, and system configuration information."
                )
                CustomStyledTextItalic(
                    title = "- Location Data.",
                    content = "We collect location data such as information about your" +
                            "device's location, which can be either precise or imprecise. How much" +
                            "information we collect depends on the type and settings of the device you use" +
                            "te access the Services. For example, we may use GPS and other technologies" +
                            "te collect geolocation data that tells us your current location (based on your IP" +
                            "address). You can opt out of allowing us to collect this information either by" +
                            "refusing access to the information or by disabling your Location setting on your" +
                            "device. However, if you choose to opt out, you may not be able to use certain" +
                            "aspects of the Services."
                )

                TitleTxt(a = "2. HOW DO WE PROCESS YOUR INFORMATION?")
                ContentTxt(
                    a = "In Short: We process your information to provide, improve, and administer our\n" +
                            "\n" +
                            "Services, communicate with you, for security and fraud prevention, and to comply\n" +
                            "with law. We may also process your information for other purposes with your consent.\n" +
                            "\n"
                )
                Text(
                    text = "We process your personal information for a variety of reasons, depending on how you interact with our Services, including:\n",
                    fontWeight = FontWeight.Bold
                )
                CustomStyledText(
                    title = "- To facilitate account creation and authentication and otherwise manage user accounts.",
                    content = "We may process your information so you can create and log in to your account, as well as keep your account in working order."
                )
                TitleTxt(a = "3. WHAT LEGAL BASES DO WE RELY ON TO PROCESS YOUR INFORMATION?")
                ContentTxt(
                    a = "in Short: We only process your personal information when we believe it is necessary\n" +
                            "and we have a valid legal reason (i.e., legal basis) to do so under applicable law, like\n" +
                            "with your consent, to comply with laws, to provide you with services to enter into or\n" +
                            "fulfill our contractual obligations, to protect your rights, or fo fulfill our legitimate\n" +
                            "business interests.\n" +
                            "\n" +
                            "We may process your information if you have given us specific permission (i.¢.,\n" +
                            "express consent) to use your personal information for a specific purpose, or in\n" +
                            "situations where your permission can be inferred (i.¢., implied consent). You\n" +
                            "can withdraw your consent at any time.\n" +
                            "\n" +
                            "In some exceptional cases, we may be legally permitted under applicable law to\n" +
                            "process your information without your consent, including, for example:\n" +
                            "\n" +
                            "= If collection is clearly in the interests of an individual and consent cannot be\n" +
                            "obtained in a timely way\n" +
                            "\n" +
                            "For investigations and fraud detection and prevention\n" +
                            "\n" +
                            "For business transactions provided certain conditions are met\n" +
                            "\n" +
                            "If it is contained in a witness statement and the collection is necessary to\n" +
                            "assess, process, or settle an insurance claim\n" +
                            "\n" +
                            "For identifying injured, ill, or deceased persons and communicating with next\n" +
                            "of kin\n" +
                            "\n" +
                            "If we have reasonable grounds to believe an individual has been, is, or may be\n" +
                            "victim of financial abuse\n" +
                            "\n" +
                            "If it is reasonable to expect collection and use with consent would compromise\n" +
                            "the availability or the accuracy of the information and the collection is\n" +
                            "reasonable for purposes related to investigating a breach of an agreement or a\n" +
                            "contravention of the laws of Canada or a province\n" +
                            "\n" +
                            "If disclosure is required to comply with a subpoena, warrant, court order, or\n" +
                            "rules of the court relating to the production of records\n" +
                            "\n" +
                            "If it was produced by an individual in the course of their employment, business,\n" +
                            "or profession and the collection is consistent with the purposes for which the\n" +
                            "information was produced\n" +
                            "\n" +
                            "If the collection is solely for journalistic, artistic, or literary purposes\n" +
                            "\n" +
                            "If the information is publicly available and is specified by the regulations"
                )
                TitleTxt(a = "4. WHEN AND WITH WHOM DO WE SHARE YOUR PERSONAL INFORMATION?")
                ContentTxt(
                    a = "In Short: We may share information in specific situations described in this section\n" +
                            "and/or with the following third parties.\n" +
                            "\n" +
                            "We may need to share your personal information in the following situations:\n" +
                            "\n" +
                            "= Business Transfers. We may share or transfer your information in connection\n" +
                            "with, or during negotiations of, any merger, sale of company assets, financing,\n" +
                            "or acquisition of all or a portion of our business to another company.\n" +
                            "\n" +
                            "= When we use Google Maps Platform APIs. We may share your information\n" +
                            "with certain Google Maps Platform APIs (e.g., Google Maps API, Places API).\n" +
                            "We obtain and store on your device (\"cache\") your location. You may revoke\n" +
                            "your consent anytime by contacting us at the contact details provided at the\n" +
                            "end of this document."
                )
                TitleTxt(a = "5. DO WE USE COOKIES AND OTHER TRACKING TECHNOLOGIES?")
                ContentTxt(
                    a = "In Short: We may use cookies and other tracking technologies to collect and store\n" +
                            "your information.\n" +
                            "\n" +
                            "We may use cookies and similar tracking technologies (like web beacons and pixels)\n" +
                            "to access or store information. Specific information about how we use such\n" +
                            "technologies and how you can refuse certain cookies is set out in our Cookie Notice."
                )
                TitleTxt(a = "6. HOW LONG DO WE KEEP YOUR INFORMATION?")
                ContentTxt(
                    a = "in Short: We keep your information for as long as necessary to fulfill the purposes\n" +
                            "outlined in this privacy notice unless otherwise required by law.\n" +
                            "\n" +
                            "We will only keep your personal information for as long as it is necessary for the\n" +
                            "purposes set out in this privacy notice, unless a longer retention period is required or\n" +
                            "permitted by law (such as tax, accounting, or other legal requirements). No purpose\n" +
                            "in this notice will require us keeping your personal information for longer than the\n" +
                            "period of time in which users have an account with us.\n" +
                            "\n" +
                            "When we have no ongoing legitimate business need to process your personal\n" +
                            "information, we will either delete or anonymize such information, or, if this is not\n" +
                            "possible (for example, because your personal information has been stored in backup\n" +
                            "archives), then we will securely store your personal information and isolate it from\n" +
                            "any further processing until deletion is possible."
                )
                TitleTxt(a = "7. HOW DO WE KEEP YOUR INFORMATION SAFE?")
                ContentTxt(
                    a = "In Short: We aim to protect your personal information through a system of\n" +
                            "organizational and technical security measures.\n" +
                            "\n" +
                            "We have implemented appropriate and reasonable technical and organizational\n" +
                            "security measures designed to protect the security of any personal information we\n" +
                            "process. However, despite our safeguards and efforts to secure your information, no\n" +
                            "electronic transmission over the Internet or information storage technology can be\n" +
                            "guaranteed to be 100% secure, so we cannot promise or guarantee that hackers,\n" +
                            "cybercriminals, or other unauthorized third parties will not be able to defeat our\n" +
                            "security and improperly collect, access, steal, or modify your information. Although\n" +
                            "we will do our best to protect your personal information, transmission of personal\n" +
                            "information to and from our Services is at your own risk. You should only access the\n" +
                            "Services within a secure environment."
                )
                TitleTxt(a = "8. DO WE COLLECT INFORMATION FROM MINORS?")
                ContentTxt(
                    a = "in Short: We do not knowingly collect data from or market fo children under 18 years\n" +
                            "of age.\n" +
                            "\n" +
                            "We do not knowingly solicit data from or market to children under 18 years of age. By\n" +
                            "using the Services, you represent that you are at least 18 or that you are the parent\n" +
                            "or guardian of such a minor and consent to such minor dependent’s use of the\n" +
                            "Services. If we learn that personal information from users less than 18 years of age\n" +
                            "has been collected, we will deactivate the account and take reasonable measures to\n" +
                            "promptly delete such data from our records. If you become aware of any data we\n" +
                            "may have collected from children under age 18, please contact us at"
                )
                TitleTxt(a = "9. WHAT ARE YOUR PRIVACY RIGHTS?")
                ContentTxt(
                    a = "In Short: in some regions, such as Canada , you have rights that allow you greater\n" +
                            "access fo and control over your personal information. You may review, change, or\n" +
                            "terminate your account at any time.\n" +
                            "\n" +
                            "In some regions (like Canada), you have certain rights under applicable data\n" +
                            "protection laws. These may include the right (i) to request access and obtain a copy\n" +
                            "of your personal information, (ii) to request rectification or erasure; (iii) to restrict the\n" +
                            "processing of your personal information; {iv) if applicable, to data portability; and (v)\n" +
                            "not to be subject to automated decision-making. In certain circumstances, you may\n" +
                            "also have the right to object to the processing of your personal information. You can\n" +
                            "make such a request by contacting us by using the contact details provided in the\n" +
                            "section \"HOW CAN YOU CONTACT US ABOUT THIS NOTICE?\" below.\n" +
                            "\n" +
                            "We will consider and act upon any request in accordance with applicable data\n" +
                            "protection laws.\n" +
                            "\n" +
                            "Withdrawing your consent: If we are relying on your consent to process your\n" +
                            "personal information, which may be express and/or implied consent depending on\n" +
                            "the applicable law, you have the right to withdraw your consent at any time. You can\n" +
                            "withdraw your consent at any time by contacting us by using the contact details\n" +
                            "provided in the section \"HOW CAN YOU CONTACT US ABOUT THIS NOTICE?\"\n" +
                            "below.\n" +
                            "\n" +
                            "However, please note that this will not affect the lawfulness of the processing before\n" +
                            "its withdrawal nor, when applicable law allows, will it affect the processing of your\n" +
                            "personal information conducted in reliance on lawful processing grounds other than\n" +
                            "consent.\n" +
                            "\n" +
                            "Account Information\n" +
                            "\n" +
                            "If you would at any time like to review or change the information in your account or\n" +
                            "terminate your account, you can:\n" +
                            "\n" +
                            "= Log in to your account settings and update your user account.\n" +
                            "\n" +
                            "Upon your request to terminate your account, we will deactivate or delete your\n" +
                            "account and information from our active databases. However, we may retain some\n" +
                            "information in our files to prevent fraud, troubleshoot problems, assist with any\n" +
                            "investigations, enforce our legal terms and/or comply with applicable legal\n" +
                            "requirements.\n" +
                            "\n" +
                            "If you have questions or comments about your privacy rights, you may email us at\n" +
                            "z53gu@uwaterloo.ca."
                )

                TitleTxt(a = "10. CONTROLS FOR DO-NOT-TRACK FEATURES")
                ContentTxt(
                    a = "Most web browsers and some mobile operating systems and mobile applications\n" +
                            "include a Do-Not-Track (\"DNT\") feature or setting you can activate to signal your\n" +
                            "privacy preference not to have data about your online browsing activities monitored\n" +
                            "and collected. At this stage no uniform technology standard for recognizing and\n" +
                            "implementing DNT signals has been finalized. As such, we do not currently respond\n" +
                            "to DNT browser signals or any other mechanism that automatically communicates\n" +
                            "your choice not to be tracked online. If a standard for online tracking is adopted that\n" +
                            "we must follow in the future, we will inform you about that practice in a revised\n" +
                            "version of this privacy notice."
                )

                TitleTxt(a = "11. DO WE MAKE UPDATES TO THIS NOTICE?")
                ContentTxt(
                    a = "in Short: Yes, we will update this notice as necessary to stay compliant with relevant\n" +
                            "Jaws.\n" +
                            "\n" +
                            "We may update this privacy notice from time to time. The updated version will be\n" +
                            "indicated by an updated \"Revised\" date and the updated version will be effective as\n" +
                            "soon as it is accessible. If we make material changes to this privacy notice, we may\n" +
                            "notify you either by prominently posting a notice of such changes or by directly\n" +
                            "\n" +
                            "sending you a notification. We encourage you to review this privacy notice frequently\n" +
                            "to be informed of how we are protecting your information."
                )

                TitleTxt(a = "12. HOW CAN YOU CONTACT US ABOUT THIS NOTICE?")
                ContentTxt(
                    a = "If you have questions or comments about this notice, you may contact us by post at:\n" +
                            "\n" +
                            "GreenTrace"
                )
                TitleTxt(
                    a = "13. HOW CAN YOU REVIEW, UPDATE, OR DELETE THE\n" +
                            "DATA WE COLLECT FROM YOU?"
                )
                ContentTxt(
                    a = "Based on the applicable laws of your country, you may have the right to request\n" +
                            "access to the personal information we collect from you, change that information, or\n" +
                            "delete it. To request to review, update, or delete your personal information, please fill\n" +
                            "out and submit a data subject access request.\n" +
                            "\n" +
                            "This privacy policy was created using Termly's Privacy Policy Generator."
                )
            }
        }
    }
}

@Composable
fun TitleTxt(a: String) {
    return Column {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = a.uppercase(), fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SecondaryTitleTxt(a: String) {
    return Column {
        Text(text = a.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ContentTxt(a: String) {
    return Text(text = a, fontSize = 14.sp)
}

@Composable
fun CustomStyledText(title: String, content: String) {
    val styledText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(title)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append(content)
        }
    }

    Text(text = styledText, fontSize = 14.sp)
}

@Composable
fun CustomStyledTextItalic(title: String, content: String) {
    val styledText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
            append(title)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append(content)
        }
    }

    Text(text = styledText, fontSize = 14.sp)
}