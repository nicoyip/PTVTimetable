package com.example.ptvtimetable.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ptvtimetable.R
import com.example.ptvtimetable.data.repository.PTVRepository
import com.example.ptvtimetable.databinding.FragmentTransformBinding
import com.example.ptvtimetable.databinding.ItemTransformBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.collections.sorted

/**
 * Fragment that displays PTV timetable departures in real-time.
 */
class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransformViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[TransformViewModel::class.java]
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewTransform
        val adapter = DepartureAdapter()
        recyclerView.adapter = adapter

        // Collect UI state from StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Show/hide loading indicator
                binding.progressBar?.isVisible = state.isLoading

                // Show/hide empty state
                binding.textEmptyState?.isVisible = !state.isLoading &&
                        state.departures.isEmpty() &&
                        state.error == null

                // Show/hide error
                binding.textError?.isVisible = state.error != null
                binding.textError?.text = state.error

                // Update header if route info is available
                if (state.routeName.isNotEmpty()) {
                    binding.textHeader?.text =
                        "${state.routeName}: ${state.fromStop} → ${state.toStop}"
                    binding.textHeader?.isVisible = true
                }

                // Update adapter
                adapter.submitList(state.departures)

                // Show error snackbar if needed
                if (state.error != null && !state.isLoading) {
                    Snackbar.make(root, state.error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry) {
                            // Retry with last known configuration
                            if (state.routeName.isNotEmpty()) {
                                viewModel.loadDepartures(
                                    routeType = "0", // Default to tram
                                    routeName = state.routeName,
                                    fromStop = state.fromStop,
                                    toStop = state.toStop
                                )
                            }
                        }
                        .show()
                }
            }
        }

        // Show configuration dialog on first launch
        if (savedInstanceState == null) {
            showConfigurationDialog()
        }

        return root
    }

    private fun showConfigurationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_departure_config, null)
        val repository = PTVRepository()

        // Find views
        val routeTypeDropdown =
            dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_route_type)
        val routeNameDropdown =
            dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_route_name)
        val fromStopDropdown =
            dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_from_stop)
        val toStopDropdown =
            dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_to_stop)
        val progressRoutes = dialogView.findViewById<ProgressBar>(R.id.progress_routes)
        val progressStops = dialogView.findViewById<ProgressBar>(R.id.progress_stops)

        // Route types with their display names
        val routeTypes = mapOf(
            "Train" to "1",
            "Tram" to "0",
            "Bus" to "2",
            "V/Line" to "3",
            "Night Bus" to "4"
        )

        // Setup route type dropdown
        val routeTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            routeTypes.keys.toList()
        )
        routeTypeDropdown.setAdapter(routeTypeAdapter)
        routeTypeDropdown.setText("Train", false)

        var selectedRouteType = "1" // Default to train
        var availableRoutes = mutableMapOf<String, Int>() // Route label to Route ID mapping
        var selectedRouteId: Int? = null
        var availableStops = listOf<com.example.ptvtimetable.data.models.Stop>()

        // Load routes when route type is selected
        routeTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedType = routeTypes.keys.toList()[position]
            selectedRouteType = routeTypes[selectedType] ?: "1"

            // Clear dependent dropdowns
            routeNameDropdown.setText("", false)
            fromStopDropdown.setText("", false)
            toStopDropdown.setText("", false)
            availableRoutes.clear()
            availableStops = emptyList()
            selectedRouteId = null

            // Load routes for this type
            progressRoutes.isVisible = true
            lifecycleScope.launch {
                try {
                    val routeTypeInt = selectedRouteType.toIntOrNull()
                    val result = repository.getAllRoutes(routeTypeInt)

                    result.onSuccess { response ->
                        Timber.d("Route API response: ${response.routes.size} routes")

                        // Build route map with route names as keys, filtering out any null or empty names
                        availableRoutes = response.routes
                            .mapNotNull { route ->
                                val routeName = route.label  // Use route_name (label field)
                                if (!routeName.isNullOrBlank()) {
                                    routeName to route.id
                                } else {
                                    null
                                }
                            }
                            .toMap()
                            .toMutableMap()

                        // Update dropdown with route names
                        val routeNames = availableRoutes.keys.sorted().toList()
                        Timber.d("Filtered to ${routeNames.size} valid routes")

                        val routeAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            routeNames
                        )
                        routeNameDropdown.setAdapter(routeAdapter)
                        routeNameDropdown.isEnabled = true
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load routes")
                        Snackbar.make(
                            dialogView,
                            "Error loading routes: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    progressRoutes.isVisible = false
                } catch (e: Exception) {
                    progressRoutes.isVisible = false
                    Snackbar.make(
                        dialogView,
                        "Error loading routes: ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Load stops when route is selected
        routeNameDropdown.setOnItemClickListener { _, _, _, _ ->
            val selectedRouteName = routeNameDropdown.text.toString()
            selectedRouteId = availableRoutes[selectedRouteName]

            // Clear stop dropdowns
            fromStopDropdown.setText("", false)
            toStopDropdown.setText("", false)

            selectedRouteId?.let { routeId ->
                progressStops.isVisible = true
                lifecycleScope.launch {
                    try {
                        // First, need to get a departure to determine direction
                        // For simplicity, we'll get stops for all directions
                        val routeTypeInt = selectedRouteType.toIntOrNull() ?: 0

                        // Try to get stops without direction first
                        val result = repository.getStopsOnRoute(
                            routeId = routeId,
                            routeType = routeTypeInt,
                            directionId = null
                        )

                        result.onSuccess { response ->
                            availableStops = response.stopsPattern

                            // Update both dropdowns with stop names, filtering out null or empty labels
                            val stopNames = availableStops
                                .mapNotNull { it.label }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .sorted()
                                .toList()
                            val stopAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                stopNames
                            )
                            fromStopDropdown.setAdapter(stopAdapter)
                            toStopDropdown.setAdapter(stopAdapter)
                            fromStopDropdown.isEnabled = true
                            toStopDropdown.isEnabled = true
                        }.onFailure { e ->
                            Snackbar.make(
                                dialogView,
                                "Error loading stops: ${e.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        progressStops.isVisible = false
                    } catch (e: Exception) {
                        progressStops.isVisible = false
                        Snackbar.make(
                            dialogView,
                            "Error loading stops: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Trigger initial load for Train routes
        lifecycleScope.launch {
            delay(100) // Small delay to ensure view is ready
            progressRoutes.isVisible = true
            try {
                val result = repository.getAllRoutes(1) // Train

                result.onSuccess { response ->
                    Timber.d("Initial train routes response: ${response.routes.size} routes")
                    response.routes.take(3).forEach { route ->
                        Timber.d("Sample route: id=${route.id}, routeName=${route.label}, routeNumber=${route.shortLabel}")
                    }

                    availableRoutes = response.routes
                        .mapNotNull { route ->
                            val routeName = route.label  // Use route_name (label field)
                            if (!routeName.isNullOrBlank()) {
                                routeName to route.id
                            } else {
                                null
                            }
                        }
                        .toMap()
                        .toMutableMap()

                    val routeNames = availableRoutes.keys.sorted().toList()

                    Timber.d("Loaded ${routeNames.size} train routes: ${routeNames.take(5)}")

                    val routeAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        routeNames
                    )
                    routeNameDropdown.setAdapter(routeAdapter)
                    routeNameDropdown.isEnabled = true
                }.onFailure { e ->
                    Timber.e(e, "Failed to load initial train routes")
                    Snackbar.make(
                        dialogView,
                        "Error loading train routes: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                progressRoutes.isVisible = false
            } catch (e: Exception) {
                Timber.e(e, "Exception loading initial train routes")
                progressRoutes.isVisible = false
                Snackbar.make(
                    dialogView,
                    "Error loading train routes: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        // Show dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Configure Departures")
            .setView(dialogView)
            .setPositiveButton("Load") { dialog, _ ->
                val routeType = selectedRouteType
                val routeName = routeNameDropdown.text.toString()
                val fromStop = fromStopDropdown.text.toString()
                val toStop = toStopDropdown.text.toString()

                if (routeName.isNotBlank() && fromStop.isNotBlank() && toStop.isNotBlank()) {
                    viewModel.loadDepartures(routeType, routeName, fromStop, toStop)
                    dialog.dismiss()
                } else {
                    Snackbar.make(
                        dialogView,
                        "Please fill in all fields",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class DepartureAdapter :
        ListAdapter<DepartureItem, DepartureViewHolder>(object :
            DiffUtil.ItemCallback<DepartureItem>() {

            override fun areItemsTheSame(oldItem: DepartureItem, newItem: DepartureItem): Boolean =
                oldItem.scheduledTime == newItem.scheduledTime &&
                        oldItem.platform == newItem.platform

            override fun areContentsTheSame(
                oldItem: DepartureItem,
                newItem: DepartureItem
            ): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartureViewHolder {
            val binding = ItemTransformBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DepartureViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DepartureViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    class DepartureViewHolder(private val binding: ItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DepartureItem) {
            with(binding) {
                textRouteLabel.text = item.routeLabel
                textScheduledTime.text = item.scheduledTime
                textMinutesUntil.text = item.minutesUntil

                // Platform
                if (item.platform != null) {
                    chipPlatform.text = root.context.getString(
                        R.string.platform,
                        item.platform
                    )
                    chipPlatform.isVisible = true
                } else {
                    chipPlatform.isVisible = false
                }

                // Express badge
                chipExpress.isVisible = item.isExpress

                // Color the route indicator based on route type
                val color = when (item.routeType) {
                    0 -> R.color.ptv_tram_green
                    1 -> R.color.ptv_train_blue
                    2 -> R.color.ptv_bus_orange
                    3 -> R.color.ptv_vline_purple
                    4 -> R.color.ptv_night_bus_white
                    else -> R.color.ptv_train_blue
                }
                routeIndicator.setBackgroundColor(
                    ContextCompat.getColor(root.context, color)
                )
            }
        }
    }
}