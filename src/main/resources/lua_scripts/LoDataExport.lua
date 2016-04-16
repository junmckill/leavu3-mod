
local function addScriptDir(path)
    package.path = package.path .. ";" .. path .. "/?.lua" .. ";" .. path .. "/?.dll"
end

addScriptDir(lfs.writedir() .. "/Scripts")
addScriptDir(lfs.writedir() .. "/LuaSocket")
addScriptDir(lfs.currentdir() .. "/Scripts")
addScriptDir(lfs.currentdir() .. "/LuaSocket")

local JSON = require 'dkjson'

-- Because dkjson is simply much much too slow we need to do this :(

dcs_remote_last_export_model_time = 0.0
dcs_remote_export_cache = {}
setmetatable(dcs_remote_export_cache, { __tojson = dcs_remote_export_cache_to_json })
local buffer = {}
local buffer_len = 0
local byteConcatBuffer = {}
local byteConcatBuffer_len = 0

local character_escapes = {}
character_escapes[string.byte("\b")] = "\\b"
character_escapes[string.byte("\f")] = "\\f"
character_escapes[string.byte("\n")] = "\\n"
character_escapes[string.byte("\r")] = "\\r"
character_escapes[string.byte("\t")] = "\\t"
character_escapes[string.byte("\"")] = "\\\""
character_escapes[string.byte("\\")] = "\\\\"

function needs_escape(str)
    for idx = 1, #str do
        local char = str:byte(idx)
        if character_escapes(char) then
            return true
        end
    end
    return false
end

function escape_string(str)

    if not needs_escape(str) then
        return str
    end

    byteConcatBuffer_len = 0

    for idx = 1, #str do
        local char = str:byte(idx)
        local escape = character_escapes(char)
        if escape then
            byteConcatBuffer[byteConcatBuffer_len+1] = string.byte(escape, 1)
            byteConcatBuffer[byteConcatBuffer_len+1] = string.byte(escape, 2)
        else
            byteConcatBuffer[byteConcatBuffer_len+1] = char
        end
    end
    return table.concat(byteConcatBuffer, 0, byteConcatBuffer_len)
end

function dcs_remote_export_cache_to_json(data)

    buffer_len = 0

    for key, value in pairs(data) do
        if type(key) == 'string' then
            table.insert(string_keys, key)
        elseif type(key) == 'number' then
            table.insert(number_keys, key)
            if key <= 0 or key >= math.huge then
                number_keys_must_be_strings = true
            elseif not maximum_number_key or key > maximum_number_key then
                maximum_number_key = key
            end
        end
    end

    setmetatable(data, nil)
    local out = JSON.encode(data)
    setmetatable(data, { __tojson = dcs_remote_export_cache_to_json })
    return out
end

function dcs_remote_export_data()

    -- only export if we're alive
    if (LoGetPlayerPlaneId()) then

        local t = LoGetModelTime()
        if t ~= dcs_remote_last_export_model_time then
            dcs_remote_last_export_model_time = t
                
            local _pitch, _roll, _heading = LoGetADIPitchBankYaw()
            dcs_remote_export_cache.metaData = {
                version = LoGetVersionInfo(),
                missionStartTime = LoGetMissionStartTime(),
                modelTime = LoGetModelTime(),
                pilotName = LoGetPilotName(),
                playerPlaneId = LoGetPlayerPlaneId(),
                camPos = LoGetCameraPosition(),
                selfData = LoGetSelfData()
            }

            dcs_remote_export_cache.flightModel = {
                altitudeAboveSeaLevel = LoGetAltitudeAboveSeaLevel(),
                altitudeAboveGroundLevel = LoGetAltitudeAboveGroundLevel(),
                pitch = _pitch,
                roll = _roll,
                heading = _heading,
                IAS = LoGetIndicatedAirSpeed(),
                TAS = LoGetTrueAirSpeed(),
                AOA = LoGetAngleOfAttack(),
                acc = LoGetAccelerationUnits(),
                vv = LoGetVerticalVelocity(),
                mach = LoGetMachNumber(),
                magneticYaw = LoGetMagneticYaw(),
                glideDeviation = LoGetGlideDeviation(),
                sideDeviation = LoGetSideDeviation(),
                slipBallPosition = LoGetSlipBallPosition(),
                atmospherePressure = LoGetBasicAtmospherePressure(),
                angularVelocity = LoGetAngularVelocity(),
                vectorVelocity = LoGetVectorVelocity(),
                windVectorVelocity = LoGetVectorWindVelocity()
            }

            dcs_remote_export_cache.indicators = {
                engines  = LoGetEngineInfo(),
                HSI      = LoGetControlPanel_HSI(),
                nav      = LoGetNavigationInfo(),
                failures = LoGetMCPState(),
                mech     = LoGetMechInfo(),
                beacon   = LoGetRadioBeaconsStatus()
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

        end
    end
    return dcs_remote_export_cache
end
