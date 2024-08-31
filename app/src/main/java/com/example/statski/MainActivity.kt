package com.example.statski

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun ReadJSONFromAssets(context: Context, path: String): String {
    val identifier = "[ReadJSON]"
    try {
        val file = context.assets.open("$path")
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
        val jsonString = stringBuilder.toString()
        Log.i(
            identifier,
            "JSON as String: $jsonString.",
        )
        return jsonString
    } catch (e: Exception) {
        Log.e(identifier,"Error reading JSON: $e.",)
        e.printStackTrace()
        return ""
    }
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModelAthlete : AthletesViewModel by viewModels()
    private val viewModelSlope : SlopesViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)


        // Setting up toolbar and navigation drawer
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null){
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.nav_home)
        }
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AlarmChannel"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("alarm_channel", name, importance).apply {
                description = descriptionText
            }
            val NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            NotificationManager.createNotificationChannel(channel)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM),
                0
            )
        }
        // Functions to parse and read JSON (generated with a python script)
        fun readJSON(filepath: String, AthletesMap: MutableMap<String, Athlete>){
            val outputString: String = ReadJSONFromAssets(this, filepath)
            if (outputString.isEmpty()) {
                Log.e("MainActivity", "Failed to read JSON file: $filepath")
                return
            }

            try {
                val obj = JSONArray(outputString)
                for (i in 0 until obj.length()) {
                    val race = obj.getJSONArray(i)
                    for (j in 0 until race.length()) {
                        val athlinfo = race.getJSONObject(j)
                        val year: String = athlinfo.getString("year_of_birth")
                        // Reading information from JSON and saving it in Athlete attribute
                        val athlInstance = Athlete(
                            name = athlinfo.getString("name"),
                            nation = athlinfo.getString("nationality"),
                            birth = year.toInt()
                        )

                        if (!AthletesMap.containsKey(athlInstance.name)) {
                            AthletesMap[athlInstance.name] = athlInstance
                        }

                        val prfm = Performance(
                            position = athlinfo.getString("position"),
                            total_time = athlinfo.getString("total_time"),
                            cup_points = athlinfo.getString("cup_points"),
                            run1 = athlinfo.optString("run1"),
                            run2 = athlinfo.optString("run2"),
                            place = athlinfo.getString("place"),
                            date = athlinfo.getString("date"),
                            category = athlinfo.getString("race_type")
                        )
                        AthletesMap[athlInstance.name]?.performance_list?.add(prfm)
                    }
                }
            }
            catch (e: Exception) {
                Log.e("MainActivity", "Error parsing JSON file: $filepath", e)
            }
            Log.d("MainActivity", "Number of athletes in map: ${AthletesMap.size}")
        }
        fun readSlopes(filepath: String, SlopesMap: MutableMap<String, Slope>){
            val outputString: String = ReadJSONFromAssets(this, filepath)
            if (outputString.isEmpty()) {
                Log.e("MainActivity", "Failed to read JSON file [SLOPE]: $filepath")
                return
            }

            try {
                val obj = JSONArray(outputString)
                for (i in 0 until obj.length()) {
                    val race = obj.getJSONArray(i)
                    for (j in 0 until race.length()) {
                        val slopeinfo = race.getJSONObject(j)
                        val SlopeInstance = Slope(
                            name = slopeinfo.getString("name"),
                            location = slopeinfo.getString("location"),
                            mountain = slopeinfo.getString("mountain"),
                            nation = slopeinfo.getString("nation"),
                            race_host = slopeinfo.getString("race_host"),
                            length = slopeinfo.getString("length"),
                            start_high = slopeinfo.getString("start"),
                            end_high = slopeinfo.getString("end"),
                            steep = slopeinfo.getString("steep"),
                            vertical_drop = slopeinfo.getString("vertical_drop")
                        )

                        if (!SlopesMap.containsKey(SlopeInstance.name)) {
                            SlopesMap[SlopeInstance.name] = SlopeInstance
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing JSON file: $filepath", e)
            }
            Log.d("MainActivity", "Number of Slopes in map: ${SlopesMap.size}")
        }
        fun readRaces(filepath: String, RacesMap: MutableList<Race>){
            val outputString: String = ReadJSONFromAssets(this, filepath)
            if (outputString.isEmpty()) {
                Log.e("MainActivity", "Failed to read JSON file [RACE]: $filepath")
                return
            }

            try {
                val obj = JSONArray(outputString)
                for (i in 0 until obj.length()) {
                    val race = obj.getJSONArray(i)
                    for (j in 0 until race.length()) {
                        val raceinfo = race.getJSONObject(j)
                        val RaceInstance = Race(
                            date = raceinfo.getString("date"),
                            place = raceinfo.getString("place"),
                            nation = raceinfo.getString("nation"),
                            race_type = raceinfo.getString("race_type")
                        )
                        RacesMap.add(RaceInstance)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing JSON file: $filepath", e)
            }
            Log.d("MainActivity", "Number of Races in map: ${RacesMap.size}")
        }


        // Data loading
        val AthletesMap = mutableMapOf<String, Athlete>()
        val SlopesMap = mutableMapOf<String, Slope>()
        val Calendar = mutableListOf<Race>()
        try {
            readJSON("speed_race.json", AthletesMap)
            readJSON("tech_race.json", AthletesMap)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading JSON files [ATHLETES]", e)
        }
        try{
            readSlopes("slopes.json", SlopesMap)
        } catch(e: Exception) {
            Log.e("MainActivity", "Error reading JSON files [SLOPES]", e)
        }
        try{
            readRaces("calendar.json", Calendar)
        } catch(e: Exception) {
            Log.e("MainActivity", "Error reading JSON files [RACES]", e)
        }

        // Addint Maps to viewmodel
        viewModelAthlete.setAthletesMap(AthletesMap)
        viewModelSlope.setSlopesMap(SlopesMap)
        viewModelAthlete.setCalendar(Calendar)
        Log.d("MainActivity", "Gare di Sofia Goggia: ${AthletesMap["GOGGIA Sofia"]?.performance_list}")
    }

    // Functions for navigation between fragments
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.wc_slopes -> replaceFragment(WCSlopesFragment())
            R.id.athletes -> replaceFragment(AthletesFragment())
            // Fragment for athletes comparison
            R.id.logout -> replaceFragment(ProfileFragment())
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    fun onBackPressedDispatcher() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        } else{
            onBackPressedDispatcher.onBackPressed()
        }
    }
}