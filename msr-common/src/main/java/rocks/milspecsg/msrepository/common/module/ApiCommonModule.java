/*
 *   MSRepository - MilSpecSG
 *   Copyright (C) 2019 Cableguy20
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package rocks.milspecsg.msrepository.common.module;

import com.google.inject.AbstractModule;
import rocks.milspecsg.msrepository.api.util.DateFormatService;
import rocks.milspecsg.msrepository.api.util.TimeConversionService;
import rocks.milspecsg.msrepository.common.util.CommonDateFormatService;
import rocks.milspecsg.msrepository.common.util.CommonTimeConversionService;

public class ApiCommonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DateFormatService.class).to(CommonDateFormatService.class);
        bind(TimeConversionService.class).to(CommonTimeConversionService.class);
    }
}
