package com.example.statski

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.statski.databinding.FragmentAthletesBinding
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import com.example.statski.databinding.ItemAthletesLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import java.util.Locale

class AthletesFragment : Fragment() {

    // Create databinding
    private lateinit var binding: FragmentAthletesBinding
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var athletesList = mutableListOf<Athlete>()
    private lateinit var Athl_adapter: AthleteAdapter
    private var currentFilterText: String? = null
    private lateinit var db : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var user : FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        // Checking if there are already fav athletes
        db.collection(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                documents ->
                val favList = mutableListOf<String>()
                for (document in documents){
                    val favAthlete = document.getString("name")
                    if (favAthlete != null) {
                        favList.add(favAthlete)
                    }
                }

                // Adapter instance
                Athl_adapter = AthleteAdapter(requireContext(), athletesList, favList)
                binding.rvAthletesList.adapter = Athl_adapter

                // Override the item click method to open the new activity
                Athl_adapter.setOnItemClickListener(object : AthleteAdapter.OnItemClickListener{
                    override fun OnItemClick(athlete: Athlete){
                        // Handling item click

                        // Get the athlete which user chose
                        viewModel_instance.selectAthlete(athlete)
                        Log.d("AthletesFragment", "Athlete picked: ${athlete}")

                        val athlete_picked = Gson().toJson(athlete)
                        val intent = Intent(activity, AthleteStats::class.java)
                        intent.putExtra("athlete_picked", athlete_picked)
                        startActivity(intent)

                    }
                })


            }
            .addOnFailureListener{
                Athl_adapter = AthleteAdapter(requireContext(), athletesList, mutableListOf<String>())
                binding.rvAthletesList.adapter = Athl_adapter
                // Override the item click method to open the new fragment
                Athl_adapter.setOnItemClickListener(object : AthleteAdapter.OnItemClickListener{
                    override fun OnItemClick(athlete: Athlete){
                        // Handling item click

                        // Get the athlete which user chose
                        viewModel_instance.selectAthlete(athlete)
                        Log.d("AthletesFragment", "Athlete picked: ${athlete}")

                        val athlete_picked = Gson().toJson(athlete)
                        val intent = Intent(activity, AthleteStats::class.java)
                        intent.putExtra("athlete_picked", athlete_picked)
                        startActivity(intent)

                    }
                })


            }

        binding = FragmentAthletesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel_instance

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvAthletesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAthletesList.setHasFixedSize(true)

        // Get Athlets list from ViewModel
        athletesList = viewModel_instance.athletesMap.values.toMutableList()
        Log.d("AthletesFragment", "Number of athletes: ${athletesList.size}")


        // SearchView Configuration
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilterText = newText
                filterList(newText)
                return true
            }
        })
         //[Thesis] Onclicklistener for radio buttons
        binding.sortSurname.setOnCheckedChangeListener {_, isChecked->
            SortAthletes()
        }

        binding.sortButtonsAge.setOnCheckedChangeListener { _, checkedId ->
            SortAthletes()
        }

        binding.deleteFilter.setOnClickListener{
            clearFilters()
        }

    }
    // Filter field
    private fun filterList(query : String?){
        if(query!= null){
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList = athletesList.filter{it.name.lowercase(Locale.ROOT).contains(lowercase_query)}

            if(filteredList.isEmpty()){
                Toast.makeText(requireContext(), "No Athlete found", Toast.LENGTH_SHORT).show()
            } else{
                Athl_adapter.setFilteredList(filteredList)
            }
        } else Athl_adapter.setFilteredList(athletesList)
    }




    // [Thesis] Sorting athletes by name, year up or year down
    private fun SortAthletes(){

        val filteredList = if(currentFilterText.isNullOrEmpty()){
            athletesList
        } else{
            athletesList.filter{athlete->
                athlete.name.lowercase(Locale.getDefault()).contains(currentFilterText!!.lowercase(Locale.getDefault()))
            }
        }

        val sortByName = binding.sortSurname.isChecked
        val sortByYearAscending = binding.sortButtonsAge.checkedRadioButtonId == R.id.year_up
        val sortByYearDescending = binding.sortButtonsAge.checkedRadioButtonId == R.id.year_down

        val currentList = Athl_adapter.athleteList

        val sortedList = when{
            sortByName && sortByYearAscending -> {
                currentList.sortedWith(compareBy<Athlete> {it.birth}.thenBy { it.name })
            }
            sortByName && sortByYearDescending ->{
                currentList.sortedWith(compareByDescending<Athlete> {it.birth}.thenBy{it.name})
            }
            sortByYearAscending->{
                currentList.sortedBy { it.birth }
            }
            sortByYearDescending->{
                currentList.sortedByDescending { it.birth }
            }
            sortByName->{
                currentList.sortedBy { it.name }
            }

            else -> currentList
        }
        Athl_adapter.setFilteredList(sortedList)
    }

    // [Thesis] Remove filters added on AthleteList
    private fun clearFilters(){
        binding.sortSurname.isChecked = false
        binding.sortButtonsAge.clearCheck()

        filterList(currentFilterText)

    }



}


class AthleteAdapter(val context: Context, var athleteList: List<Athlete>, var favList : MutableList<String>) :
    RecyclerView.Adapter<AthleteAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: ItemAthletesLayoutBinding): RecyclerView.ViewHolder(binding.root)



    fun setFilteredList(ListAthletes : List<Athlete>){
        this.athleteList = ListAthletes
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAthletesLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var athlete = athleteList[position]
        holder.binding.athlId.text = athlete.name
        holder.binding.natId.text = athlete.nation
        holder.binding.year.text = athlete.birth.toString()
        holder.binding.executePendingBindings()



        if(favList.isNotEmpty()){
            if (athlete.name in favList){
                holder.binding.favorite.setImageResource(R.drawable.baseline_star_24)
            }
            else{
                holder.binding.favorite.setImageResource(R.drawable.baseline_star_border_24)
            }

        }



        // Item click manage
        holder.itemView.setOnClickListener{
            onItemClickListener?.OnItemClick(athlete)
        }
        val db = Firebase.firestore
        var firebaseAuth = FirebaseAuth.getInstance()

        holder.binding.favorite.setOnClickListener {
            if(athlete.name in favList){
                // If athlete is in fav list, change img to blank star and remove it from fav list
                holder.binding.favorite.setImageResource(R.drawable.baseline_star_border_24)
                favList.remove(athlete.name)
                athlete.isFav = false
                db.collection(firebaseAuth.currentUser!!.uid).document(athlete.name).delete()
                    .addOnSuccessListener {
                        Log.d("Firebase Write", "Document removed!")
                    }
                    .addOnSuccessListener {
                        Log.d("Firebase Write", "Document not removed")
                    }

            }
            else{
                // If athlete is not in fav list, change img to filled star and add it to fav list
                holder.binding.favorite.setImageResource(R.drawable.baseline_star_24)
                favList.add(athlete.name)
                athlete.isFav = true
                db.collection(firebaseAuth.currentUser!!.uid).document(athlete.name).set(athlete)
                    .addOnSuccessListener {
                        Log.d("Firebase Write", "Document added!")
                    }
                    .addOnFailureListener {
                        Log.d("Firebase Write", "Document not added")
                    }
            }

        }


    }
    override fun getItemCount(): Int {
        return athleteList.size
    }

    // Interface for item click
    interface OnItemClickListener{
            fun OnItemClick(athlete: Athlete)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }

}


