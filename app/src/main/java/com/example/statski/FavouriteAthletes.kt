package com.example.statski

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.statski.AthleteAdapter.OnItemClickListener
import com.example.statski.databinding.FragmentFavouriteAthletesBinding
import com.example.statski.databinding.ItemFavAthletesLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson

import java.util.Locale


    // [Thesis]

class FavouriteAthletes : Fragment() {

    private lateinit var binding: FragmentFavouriteAthletesBinding

    private var FavAthlList = mutableListOf<Athlete>()
    val viewModel_instance: AthletesViewModel by activityViewModels()
    private lateinit var FavAthl_adapter: FavouriteAthletesAdapter
    private var currentFilterText: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouriteAthletesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel_instance

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        // Get Athletes list from ViewModel
        FavAthlList = viewModel_instance.athletesMap.values.toMutableList()

        // Setting RV details
        binding.rvFavouriteAthletesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavouriteAthletesList.setHasFixedSize(true)

        // Setting adapter instance
        FavAthl_adapter =
            FavouriteAthletesAdapter(requireContext(), FavAthlList, mutableListOf<String>())

        // Checking if there are fav athletes
        db.collection(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener { documents ->
                val favList = mutableListOf<String>()
                for (document in documents) {
                    val favAthlete = document.getString("name")
                    if (favAthlete != null) {
                        favList.add(favAthlete)
                    }
                }
                FavAthl_adapter = FavouriteAthletesAdapter(requireContext(), FavAthlList, favList)
                binding.rvFavouriteAthletesList.adapter = FavAthl_adapter

                if (favList.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "You do not have any favourite athlete ",
                        Toast.LENGTH_LONG
                    ).show()
                }

                FavAthl_adapter.setOnItemClickListener(object :
                    FavouriteAthletesAdapter.OnItemClickListener {
                    override fun OnItemClick(athlete: Athlete) {
                        // Getting athlete selected
                        val athlete_picked = Gson().toJson(athlete)

                        val intent = Intent(activity, FocusOnFavAthlete::class.java)
                        intent.putExtra("FavListSize", favList.size.toString())
                        intent.putExtra("athlete_picked", athlete_picked)
                        startActivity(intent)
                    }
                })


            }
            .addOnFailureListener {
                FavAthl_adapter =
                    FavouriteAthletesAdapter(requireContext(), FavAthlList, mutableListOf<String>())
                binding.rvFavouriteAthletesList.adapter = FavAthl_adapter

            }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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
    }

    private fun filterList(query: String?) {
        if (query != null) {
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList =
                FavAthlList.filter { it.name.lowercase(Locale.ROOT).contains(lowercase_query) }

            if (filteredList.isEmpty()) {
                Toast.makeText(requireContext(), "No Athlete found", Toast.LENGTH_SHORT).show()
            } else {
                FavAthl_adapter.setFilteredList(filteredList)
            }
        } else FavAthl_adapter.setFilteredList(FavAthlList)
    }
}

class FavouriteAthletesAdapter(
    val context: Context,
    var athleteList: List<Athlete>,
    var favList: MutableList<String>
) :
    RecyclerView.Adapter<FavouriteAthletesAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemFavAthletesLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


    fun setFilteredList(ListAthletes: List<Athlete>) {
        this.athleteList = ListAthletes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFavAthletesLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favAthletes = athleteList.filter {
            it.name in favList
        }
        var favAthlete = favAthletes[position]
        holder.binding.athleteId.text = favAthlete.name
        holder.binding.yearId.text = favAthlete.birth.toString()
        holder.binding.nationId.text = favAthlete.nation

        holder.itemView.setOnClickListener {
            onItemClickListener?.OnItemClick(favAthlete)
        }

    }

    override fun getItemCount(): Int = favList.size

    interface OnItemClickListener {
        fun OnItemClick(athlete: Athlete)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}