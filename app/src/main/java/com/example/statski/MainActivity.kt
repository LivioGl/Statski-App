package com.example.statski

import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun ReadJSONFromAssets(context: Context, path: String): String {
    val identifier = "[ReadJSON]"
    try {
        val file = context.assets.open("$path")
//        Log.i(
//            identifier,
//            "Found File: $file.",
//        )
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
//        Log.i(
//            identifier,
//            "getJSON stringBuilder: $stringBuilder.",
//        )
        val jsonString = stringBuilder.toString()
        Log.i(
            identifier,
            "JSON as String: $jsonString.",
        )
        return jsonString
    } catch (e: Exception) {
        Log.e(
            identifier,
            "Error reading JSON: $e.",
        )
        e.printStackTrace()
        return ""
    }
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
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

        // Data loading
        val AthletesMap = mutableMapOf<String, Athlete>()

        val OutputString : String = ReadJSONFromAssets(this, "speed_race.json")
        val obj = JSONArray(OutputString)

        for(i in 0 until obj.length() ) {
            val race = obj.getJSONArray(i)
            for(j in 0 until race.length()) {
                val athlinfo = race.getJSONObject(j)
                // val year : String =
                val athl_instance = Athlete(
                    name = athlinfo.getString("name"),
                    nation = athlinfo.getString("nationality"),
                    birth = athlinfo.getString("year_of_birth")
                )

                if(!AthletesMap.containsKey(athl_instance.name)){
                    AthletesMap[athl_instance.name] = athl_instance
                }
                val performance_day = athlinfo.getString("date")
                val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                val performance_date = LocalDate.parse(performance_day, formatter)

                val prfm = Performance(
                    position = athlinfo.getString("position"),
                    total_time = athlinfo.getString("total_time"),
                    cup_points = athlinfo.getString("cup_points"),
                    run1 = athlinfo.optString("run1"),
                    run2 = athlinfo.optString("run2"),
                    place = athlinfo.getString("place"),
                    date = performance_date,
                    category = athlinfo.getString("race_type")
                )
                AthletesMap.get(athl_instance.name)!!.performance_list.add(prfm)
            }
        }
        val Goggia : Athlete? = AthletesMap.get("GOGGIA Sofia")
        if (Goggia != null) {
            println(Goggia.name)
            println(Goggia.nation)
            println(Goggia.birth)
        }



    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.wc_slopes -> replaceFragment(WCSlopesFragment())
            R.id.athletes -> replaceFragment(AthletesFragment())
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