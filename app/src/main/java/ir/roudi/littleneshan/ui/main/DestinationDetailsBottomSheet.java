package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;
import ir.roudi.littleneshan.databinding.BottomSheetDestinationDetailsBinding;

@AndroidEntryPoint
public class DestinationDetailsBottomSheet extends BottomSheetDialogFragment {

    public static final String KEY_DOES_START_NAVIGATION = "start";

    private BottomSheetDestinationDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = BottomSheetDestinationDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avoidDefaultDimBackground();

        bindUi();

        registerOnRouteClickListener();

        registerOnDismissListener();
    }

    private void avoidDefaultDimBackground() {
        if(getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void bindUi() {
        var args = DestinationDetailsBottomSheetArgs.fromBundle(getArguments());
        var address = new AddressUiModel(args.getTitle(), args.getDuration(), args.getDistance(), args.getAddress());
        binding.setAddress(address);
    }

    private void registerOnRouteClickListener() {
        binding.route.setOnClickListener(v -> {
            setStartNavigation(true);
            close();
        });
    }

    private void setStartNavigation(boolean value) {
        var navController = findNavController(DestinationDetailsBottomSheet.this);
        var backStack = navController.getPreviousBackStackEntry();
        if(backStack == null)
            return;
        var savedState = backStack.getSavedStateHandle();
        savedState.set(KEY_DOES_START_NAVIGATION, value);
    }

    private void close() {
        dismiss();
        var navController = findNavController(DestinationDetailsBottomSheet.this);
        navController.popBackStack();
    }

    private void registerOnDismissListener() {
        if(getDialog() != null) {
            getDialog().setOnDismissListener(dialog -> {
                setStartNavigation(false);
                close();
            });
        }
    }

}
