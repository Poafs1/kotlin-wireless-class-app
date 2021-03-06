package com.example.fillfeel

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.squareup.picasso.Picasso
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import javax.xml.datatype.DatatypeConstants.DAYS
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class DetailsFragment : Fragment() {
    private lateinit var mDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private val TAG: String = "DetailsFragment"

    lateinit var englishThaiTranslator: FirebaseTranslator
    lateinit var thaiEnglishTranslator: FirebaseTranslator

    private lateinit var eventId: String
    private lateinit var eventImg: String
    private lateinit var title: String

    lateinit var titleImage: ImageView
    lateinit var headerTitle: TextView
    lateinit var cardImage: CardView
    lateinit var progressBar: ProgressBar
    lateinit var currentDonated: TextView
    lateinit var eventGoal: TextView
    lateinit var currentBackers: TextView
    lateinit var periodDate: TextView
    lateinit var daysToGo: TextView
    lateinit var detailsTitle: TextView
    lateinit var detailsEventDetail: TextView
    lateinit var detailsOverview: TextView
    lateinit var detailsPlan: TextView

    lateinit var detailsPeriod: TextView
    lateinit var detailsDonation: TextView
    lateinit var detailsBackersText: TextView
    lateinit var detailsDaysText: TextView
    lateinit var detailsOverviewTitle: TextView
    lateinit var detailsPlanTitle: TextView
    lateinit var joinButton: AppCompatButton

    var savedCurrent by Delegates.notNull<Double>()
    var savedGoal by Delegates.notNull<Double>()
    var savedBacker by Delegates.notNull<Int>()
    lateinit var savedTitle: String
    lateinit var savedImg: String
    lateinit var savedPalette: String
    var savedEndDate by Delegates.notNull<Long>()

    var isSaved: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = this.arguments
        eventId = bundle?.getString("eventId", "").toString()
//        eventImg = bundle?.getString("eventImg", "").toString()

        titleImage = view!!.findViewById(R.id.detailsCardImg)
        headerTitle = view!!.findViewById(R.id.detailsHeaderTitle)
        cardImage = view!!.findViewById(R.id.detailsCard)
        progressBar = view!!.findViewById(R.id.detailsProgressBar)
        currentDonated = view!!.findViewById(R.id.detailsEventCurrentDonated)
        eventGoal = view!!.findViewById(R.id.detailsEventGoal)
        currentBackers = view!!.findViewById(R.id.detailsBackers)
        periodDate = view!!.findViewById(R.id.detailsPeriodDate)
        daysToGo = view!!.findViewById(R.id.detailsDaysToGo)
        detailsTitle = view!!.findViewById(R.id.detailsTitle)
        detailsEventDetail = view!!.findViewById(R.id.detailsEventDetail)
        detailsOverview = view!!.findViewById(R.id.detailsOverview)
        detailsPlan = view!!.findViewById(R.id.detailsPlan)

        detailsPeriod = view!!.findViewById(R.id.detailsPeriod)
        detailsDonation = view!!.findViewById(R.id.detailsDonation)
        detailsBackersText = view!!.findViewById(R.id.detailsBackersText)
        detailsDaysText = view!!.findViewById(R.id.detailsDaysText)
        detailsOverviewTitle = view!!.findViewById(R.id.detailsOverviewTitle)
        detailsPlanTitle = view!!.findViewById(R.id.detailsPlanTitle)
        joinButton = view!!.findViewById(R.id.joinButton)
    }

    fun translateToEn(view: TextView) {
        val text = view.text.toString()
        thaiEnglishTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                view.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    fun translateToTh(view: TextView) {
        val text = view.text.toString()
        englishThaiTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                view.text = translatedText
//                view.typeface = R.font.sukhumvit_bold
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    fun translateToEnButton(view: AppCompatButton) {
        val text = view.text.toString()
        thaiEnglishTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                view.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    fun translateToThButton(view: AppCompatButton) {
        val text = view.text.toString()
        englishThaiTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                view.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    fun languageChange(lang: String) {
        if (lang == "en") {
            translateToEn(detailsPeriod)
            translateToEn(detailsDonation)
            translateToEn(detailsBackersText)
            translateToEn(detailsDaysText)
            translateToEn(detailsOverviewTitle)
            translateToEn(detailsPlanTitle)
            translateToEnButton(joinButton)
        } else {
            translateToTh(detailsPeriod)
            translateToTh(detailsDonation)
            translateToTh(detailsBackersText)
            translateToTh(detailsDaysText)
            translateToTh(detailsOverviewTitle)
            translateToTh(detailsPlanTitle)
            translateToThButton(joinButton)
        }
        return
    }

    fun initTranslation() {
        val options1 = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.TH)
            .build()

        val options2 = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.TH)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build()

        englishThaiTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options1)
        englishThaiTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }

        thaiEnglishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options2)
        thaiEnglishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    @SuppressLint("ResourceType")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initTranslation()

        mDatabase = FirebaseDatabase.getInstance().getReference()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        val colorQuoteWhite = getResources().getString(R.color.colorQuoteWhite);
        val colorPrimaryPink = getResources().getString(R.color.colorPrimaryPink);

        val detailsSaved: ImageView = view!!.findViewById(R.id.detailsSaved)
        detailsSaved.setColorFilter(Color.parseColor(colorQuoteWhite))

        mDatabase
            .child("users")
            .child(user.uid).child("lang")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, databaseError.message)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        languageChange(dataSnapshot.value.toString())
                    }
                }
            })

        mDatabase.child("users").child(user.uid).child("savedEvent").child(eventId)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.e(TAG, p0.message)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        detailsSaved.setColorFilter(Color.parseColor(colorPrimaryPink))
                        isSaved = true
                    } else {
                        detailsSaved.setColorFilter(Color.parseColor(colorQuoteWhite))
                        isSaved = false
                    }
                }
            })

        //saveButton
        detailsSaved.setOnClickListener{view ->
            if (isSaved) {
                // remove from savedEvent
                mDatabase
                    .child("users")
                    .child(user.uid)
                    .child("savedEvent")
                    .child(eventId).setValue(null)
//                Snackbar.make(view, "Saved event has been remove", Snackbar.LENGTH_LONG).show();
            } else {
                // push into savedEvent
                val childUpdates: MutableMap<String, Any> = mutableMapOf()
                childUpdates.put(eventId+"/title", savedTitle)
                childUpdates.put(eventId+"/img", savedImg)
                childUpdates.put(eventId+"/palette", savedPalette)
                childUpdates.put(eventId+"/current", savedCurrent)
                childUpdates.put(eventId+"/goal", savedGoal)
                childUpdates.put(eventId+"/backer", savedBacker)
                childUpdates.put(eventId+"/endDate", savedEndDate)
//                childUpdates.put(eventId+"/timestamp", System.currentTimeMillis())
                mDatabase
                    .child("users")
                    .child(user.uid)
                    .child("savedEvent")
                    .updateChildren(childUpdates)

//                Snackbar.make(view, "Event has been saved", Snackbar.LENGTH_LONG).show();
            }
        }

        val joinButton: AppCompatButton = view!!.findViewById(R.id.joinButton)
        joinButton.setOnClickListener{view ->
            val activity = view.context as AppCompatActivity
            val fragment = DonateFragment()

            val bundle = Bundle()
            bundle.putString("eventId", eventId)
            bundle.putString("title", title)
            bundle.putString("img", savedImg)
            bundle.putString("palette", savedPalette)
            fragment.setArguments(bundle)

            activity.getSupportFragmentManager()
                .beginTransaction().replace(R.id.root_layout, fragment).addToBackStack(null).commit();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("events").child(eventId)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, databaseError.message)
                }

                @SuppressLint("SetTextI18n")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val elem = dataSnapshot.getValue(DetailsObject::class.java)

                        eventImg = elem?.img.toString()
                        Picasso.get().load(eventImg).into(titleImage)

                        headerTitle.text = elem?.tag.plus(" Program")

                        cardImage.setCardBackgroundColor(Color.parseColor(elem?.paletteImage))

                        val divide = elem?.goal?.let { elem.donate?.div(it) }
                        val calProgress = divide?.times(100)
                        if (calProgress != null) {
                            progressBar.setProgress(calProgress.roundToInt())
                        }

                        //for saved page
                        savedTitle = elem?.title.toString()
                        savedImg = elem?.img.toString()
                        savedPalette = elem?.paletteImage.toString()

                        if (elem != null) {
                            currentDonated.text = "US$ " + elem.donate?.toInt().toString()
                            savedCurrent = elem.donate?.toDouble()!!
                        }

                        if (elem != null) {
                            eventGoal.text = "goal: US$ " + elem.goal?.roundToInt().toString()
                            savedGoal = elem.goal?.toDouble()!!
                        }

                        if (elem != null) {
                            currentBackers.text = elem.backers.toString()
                            savedBacker = elem.backers!!
                        }

                        val createDate = elem?.timestamps?.let {
                            Instant.ofEpochSecond(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                        val endDate = elem?.period?.let {
                            Instant.ofEpochSecond(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                        val nowDate = LocalDateTime.now()
                        savedEndDate = elem?.period!!

                        if (endDate?.isAfter(nowDate)!!) {
                            if (createDate != null) {
                                if (endDate != null) {
                                    periodDate.text = (endDate.monthValue - createDate.monthValue).toString() + " months"
                                }
                            }
                        } else {
                            periodDate.text = "Closed"
                        }

                        val daysBetween: Long = ChronoUnit.DAYS.between(nowDate, endDate)
                        daysToGo.text = daysBetween.toString()

                        detailsTitle.text = elem?.title
                        title = elem?.title.toString()

                        detailsEventDetail.text = elem?.details

                        detailsOverview.text = elem?.overview

                        detailsPlan.text = elem?.plan
                    }
                }
            })
    }

}
