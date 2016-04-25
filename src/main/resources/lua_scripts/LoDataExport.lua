-- Because dkjson is simply much much too slow we need to do this :(

local dcs_remote_export_cache = {}
local dcs_remote_buffer = {}
local dcs_remote_buffer_len = 0
local escape_strings = {}
escape_strings[string.byte("\b")] = "\\b"
escape_strings[string.byte("\f")] = "\\f"
escape_strings[string.byte("\n")] = "\\n"
escape_strings[string.byte("\r")] = "\\r"
escape_strings[string.byte("\t")] = "\\t"
escape_strings[string.byte("\"")] = "\\\""
escape_strings[string.byte("\\")] = "\\\\"

local write_value

local function append(s)
    dcs_remote_buffer[dcs_remote_buffer_len + 1] = s
    dcs_remote_buffer_len = dcs_remote_buffer_len + 1
end

local function needs_escape(str)
    local n = string.len(str)
    for idx = 1,n  do
        local char = str:byte(idx)
        if escape_strings[char] then
            return true
        end
    end
    return false
end

local function append_escaped_string(str)

    append("\"")

    if not needs_escape(str) then
        append(str)
    else

        -- Assume DCS at least exports in utf8 format

        local n = string.len(str)
        for idx = 1,n do
            local byte = str:byte(idx)
            append(escape_strings[byte] or string.char(byte))
        end
    end

    append("\"")
end

local function append_number(data)
    if data == data and data ~= math.huge and data ~= -math.huge then
        append(tostring(data))
    else
        append("0") -- nan or inf
    end
end

local function append_boolean(data)
    append(data and "true" or "false")
end

local function isarray (tbl)
    local max, n, arraylen = 0, 0, 0
    for k,v in pairs (tbl) do
        if k == 'n' and type(v) == 'number' then
            arraylen = v
            if v > max then
                max = v
            end
        else
            if type(k) ~= 'number' or k < 1 or math.floor(k) ~= k then
                return false
            end
            if k > max then
                max = k
            end
            n = n + 1
        end
    end
    if max > 10 and max > arraylen and max > n * 2 then
        return false -- don't create an array with too many holes
    end
    return true, max
end

local function append_array(data, n)
    append("[")
    for i = 1,n  do
        if i > 1 then append(",") end
        write_value(data[i])
    end
    append("]")
end

local function append_object(data)
    append("{")
    local i = 1
    for key, value in pairs(data) do
        if i > 1 then append(",") end
        append_escaped_string(key)
        append(":")
        write_value(value)
        i = i + 1
    end
    append("}")
end

function write_value(data)

    if data == nil then
        append("null")
        return
    end

    local tpe = type(data)

    if tpe == 'string' then
        append_escaped_string(data)

    elseif tpe == 'number' then
        append_number(data)

    elseif tpe == 'boolean' then
        append_boolean(data)

    elseif tpe == 'table' then

        local isa, n = isarray(data)

        if isa then
            append_array(data, n)
        else
            append_object(data)
        end
    end
end

local function dcs_remote_export_cache_to_json(data)
    dcs_remote_buffer_len = 0
    append_object(data, "")
    return table.concat(dcs_remote_buffer, "", 1, dcs_remote_buffer_len)
end

setmetatable(dcs_remote_export_cache, { __tojson = dcs_remote_export_cache_to_json })

function dcs_remote_export_data()

    -- only export if we're alive
    local ownPlaneId = LoGetPlayerPlaneId()
    if not ownPlaneId then
        return {}
    end

    -- local _pitch, _roll, _heading = LoGetADIPitchBankYaw()
    dcs_remote_export_cache.metaData = {
        -- version = LoGetVersionInfo(),
        -- missionStartTime = LoGetMissionStartTime(),
        modelTime = LoGetModelTime(),
        -- pilotName = LoGetPilotName(),
        playerPlaneId = ownPlaneId,
        -- camPos = LoGetCameraPosition(),
        selfData = LoGetSelfData()
    }

    dcs_remote_export_cache.flightModel = {
        -- altitudeAboveSeaLevel = LoGetAltitudeAboveSeaLevel(),
        -- altitudeAboveGroundLevel = LoGetAltitudeAboveGroundLevel(),
        -- pitch = _pitch,
        -- roll = _roll,
        -- heading = _heading,
        -- IAS = LoGetIndicatedAirSpeed(),
        -- TAS = LoGetTrueAirSpeed(),
        -- AOA = LoGetAngleOfAttack(),
        -- acc = LoGetAccelerationUnits(),
        -- vv = LoGetVerticalVelocity(),
        -- mach = LoGetMachNumber(),
        -- magneticYaw = LoGetMagneticYaw(),
        -- glideDeviation = LoGetGlideDeviation(),
        -- sideDeviation = LoGetSideDeviation(),
        -- slipBallPosition = LoGetSlipBallPosition(),
        -- atmospherePressure = LoGetBasicAtmospherePressure(),
        -- angularVelocity = LoGetAngularVelocity(),
        vectorVelocity = LoGetVectorVelocity(),
        -- windVectorVelocity = LoGetVectorWindVelocity()
    }

    dcs_remote_export_cache.indicators = {
        engines  = LoGetEngineInfo(),
        -- HSI      = LoGetControlPanel_HSI(),
        nav      = LoGetNavigationInfo(),
        -- failures = LoGetMCPState(),
        -- mech     = LoGetMechInfo(),
        -- beacon   = LoGetRadioBeaconsStatus()
    }

    dcs_remote_export_cache.sensor = {
        status = LoGetSightingSystemInfo(),
        targets = {
            detected = LoGetTargetInformation(),
            locked = LoGetLockedTargetInformation(),
            tws = LoGetF15_TWS_Contacts()
        }
    }

    dcs_remote_export_cache.payload = LoGetPayloadInfo()
    dcs_remote_export_cache.route = LoGetRoute()

    dcs_remote_export_cache.electronicWarfare = {
        rwr = LoGetTWSInfo(),
        counterMeasures = LoGetSnares()
    }

    dcs_remote_export_cache.wingMen = LoGetWingInfo()
    dcs_remote_export_cache.wingTargets = LoGetWingTargets()

    return dcs_remote_export_cache
end
