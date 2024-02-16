package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;

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
        var args = DestinationDetailsBottomSheetArgs.fromBundle(getArguments());
        binding.title.setText(args.getTitle());
        binding.duration.setText(args.getDuration());
        binding.distance.setText(args.getDistance());
        binding.address.setText(args.getAddress());

        binding.route.setOnClickListener(v -> {
            setStartNavigation(true);

            if(getDialog() != null)
                getDialog().dismiss();
        });

        if(getDialog() != null) {
            getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    setStartNavigation(false);
                    dialog.dismiss();
                }
            });
        }
    }

    private void setStartNavigation(boolean value) {
        var navController = findNavController(DestinationDetailsBottomSheet.this);
        var backStack = navController.getPreviousBackStackEntry();
        if(backStack == null)
            return;
        var savedState = backStack.getSavedStateHandle();
        savedState.set(KEY_DOES_START_NAVIGATION, value);
    }
}
