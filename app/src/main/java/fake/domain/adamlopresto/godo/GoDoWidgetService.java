package fake.domain.adamlopresto.godo;

import android.content.Intent;
import androidx.annotation.Nullable;
import android.widget.RemoteViewsService;

public class GoDoWidgetService extends RemoteViewsService {

    @Nullable
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GoDoViewsFactory(getApplicationContext());
    }
}
