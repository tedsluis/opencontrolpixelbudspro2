/*
 * OpenControl for Pixel Buds Pro 2
 * Copyright (C) 2026 Ted Sluis
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package android.bluetooth

/**
 * A stub `BluetoothDevice` for `:data`'s JVM tests (`ai-sessions/0091`): the SDK stub's no-argument constructor is package-private, so this file lives in
 * `android.bluetooth`. With `unitTests.isReturnDefaultValues = true` every method of the stub returns a default — the repository only hands the object to
 * the (fake) transport, which ignores it. Lets a test run `BudsRepositoryImpl.connect()` and the ADR-044 re-open end to end instead of stopping at `NotPaired`.
 * Never used by the app.
 */
fun stubBluetoothDevice(): BluetoothDevice = BluetoothDevice()
